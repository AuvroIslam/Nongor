<div align="center">

# নোঙর · Nongor

**An offline crisis companion for Bangladesh**
No internet. No account. No server. No signal required.

*July Hackathon 2026 — Crisis Tech (Track A)*

</div>

---

*Nongor (নোঙর) means **anchor** — what holds when everything else is moving.*

When a flood takes out the tower, the phone in your pocket becomes a torch and a clock.
Nongor makes it a rescue tool instead: it calls for help phone-to-phone with no network,
routes you around water that is already over the road, tells you what to do for the bleeding,
and — the part nobody else builds — **lets a Bangla-speaking volunteer actually talk to
someone who does not speak Bangla.**

Every feature below runs on a phone in aeroplane mode. That is the whole design constraint.

---

## The problem we kept coming back to

Bangladesh's disaster response is good. The gap is not helicopters or volunteers — it is the
last two hundred metres, where a rescuer reaches a person and cannot do anything useful:

- The network is down, so nobody knows they are there.
- The road on the map is under a metre of water.
- **They do not share a language.** A volunteer from Dhaka reaches a Chakma family in
  Rangamati, a Rohingya family in Ukhiya, a Deaf woman in a shelter queue — and the first
  question, *"are you hurt?"*, cannot be asked. Google Translate has none of these languages,
  and it would need internet if it did.

That last one is why Nongor exists in the shape it does.

---

## Emergency Translation — the part we built this app around

No offline translator covers Chakma, Marma, Kokborok, Santali, Garo or Rohingya. So Nongor
does not pretend to be one. It does something that actually works with no shared language:

**1. A fixed set of the questions that matter in a rescue.** 127 phrases across first contact,
medical, rescue, reassurance, water and food, shelter, family, danger and directions — with
**276 lines of sourced translation** across six languages:

| | Chakma | Rohingya | Kokborok | Santali | Marma | Garo |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| lines | 51 | 50 | 50 | 50 | 39 | 36 |

**2. A hand-over card, split down the middle.** The top half is drawn upside down. You lay the
phone flat between you and the other person reads their half across it — nobody snatches the
phone back and forth, and both of you can see what was asked and what was answered.

**3. Every question has a pictogram and a tap-reply.** Yes / No / Don't know, a number pad, a
1–5 pain scale, a body-part list. So the conversation completes **even when the phrasebook has
no line at all** for that language. That is the actual innovation here: the fallback is not a
worse translation, it is a protocol that needs no language.

**4. A described BdSL-style gesture for every phrase**, for Deaf users and for noise.

**5. Ten guided questions build a hand-over note** — bleeding: yes, breathing: no, four people,
one child — which is triaged by the same rule engine the rest of the app uses and can be sent
straight over the mesh as an SOS. A volunteer who speaks no Chakma still walks away holding a
structured medical record.

> **On the minority-language lines.** Bangla and English are authored. Everything else is one
> of two clearly-labelled things, and the app never flattens them into each other:
>
> - **From a corpus** — Chakma, Marma and Garo lines drawn from
>   [MELD](https://data.mendeley.com/datasets/dy5dyfygbp/4) (CC BY 4.0), a published parallel
>   corpus collected from native speakers at Daffodil International University. Each line
>   carries the English sentence it was *actually* translated from, shown on screen — because
>   the closest published line to "Do you need help?" is "Can I help you?", and the volunteer
>   should see that rather than have it hidden.
> - **Unverified seed** — a small Chakma and Rohingya word list, marked
>   **যাচাই হয়নি · unverified** every time it appears.
>
> Neither is presented as verified, and the pictogram plus the yes/no protocol never depend on
> either. Verification by native speakers is the next step, not something quietly assumed — see
> [`docs/PHRASEBOOK.md`](docs/PHRASEBOOK.md).
>
> **Gemma is never used to translate these languages.** It has almost no training data for
> them and would produce fluent, confident, wrong output — the one failure mode a rescue
> phrasebook cannot have. Where the optional model *is* installed it does one narrow job:
> reading a free-text description and picking which existing phrases to show. It selects ids;
> it never writes a word of any language, and a hallucinated id is discarded.

---

## Everything else

| Feature | What it does when the network is gone |
| --- | --- |
| **Emergency call** | 999 plus the official BD short codes (1090 flood warning, 16111 Coast Guard, 333, 102, 109, 1098) bundled in the APK. Uses `ACTION_DIAL`, so it needs no call permission and never dials on its own. |
| **Mesh SOS** | Ed25519-signed SOS over Nearby Connections (Bluetooth + Wi-Fi Direct), relayed multi-hop phone to phone. A `isProductionTrusted()` gate accepts only `scheme == ed25519`, so an attacker cannot forge a "verified" message by claiming an older scheme. Forgeries are quarantined, never merged. A store-and-forward outbox holds an SOS sent with nobody in range and flushes it the moment a peer appears — "queued" never silently means "lost". |
| **SMS bridge** | After a flood the data network dies first and comes back last, but the same tower often still carries SMS — and half the handsets in a haor village are button phones. An SOS compresses to one line (`NGR1 C 24.8901,91.8712 P4 F:trp,bld N:Rahim`) that fits a single 160-character SMS, reads on any handset made this century, and pastes back into another Nongor phone to recover the exact coordinates. Nongor fills in your messaging app; you press send. It asks for no SMS permission. |
| **Safe shelter & route** | Dijkstra flood-avoiding routing over real OpenStreetMap road graphs for three districts (4,801 / 1,532 / 546 junctions), with a nationwide fallback to the nearest of **9,525** real shelters across all **64** districts. |
| **First aid** | Retrieval over a WHO/IFRC-grounded corpus, cited, in Bangla or English, with a life-threat red-flag banner. |
| **Rescue triage** | Ranks a queue of SOS reports by a transparent rule table, and shows the signal that caused each ranking — a responder has to be able to disagree with the app, and can only do that if they can see why. |
| **Community board** | Tagged area reports (road flooded, shelter full, danger) over the same signed mesh, scoped to your district so one area's news does not drown another's. |
| **Family reunion** | Your phone beacons a hashed family tag and an AES-sealed name. Only a phone with the same family code can open it, so strangers in range learn nothing. Shows direction and rough distance when a separated member's phone passes by. |
| **Coordinator summary** | A briefing where **every exact number is computed in code** — counts, coordinates, capacities. Nothing precise is ever passed through a language model to be echoed back. |
| **Full Bangla** | Both languages live side by side at every call site, so a missing translation does not compile. |
| **Drill & guide** | Practise before it happens; drill data is purged the moment a real report arrives. |

### The optional AI

An on-device model (LiteRT-LM / Gemma) can be downloaded to add free-form questions and photo
assessment. **It is genuinely optional.** Every feature above runs without it, the intro offers
a one-tap skip, and the home screen says plainly whether it is installed. We would rather the
app work on a ৳8,000 handset than demo well on a flagship.

---

## Design rules we held to

**Counts in code, never in prose.** A language model may phrase a briefing; it is never handed
a coordinate, capacity or ID to reproduce. Long numbers are exactly what they get wrong.

**Every ranking shows its reason.** No bare urgency score anywhere.

**Degrade to something, never to nothing.** No peer in range → outbox. No data → SMS. No
translation → pictogram. No GPS → district centre, clearly labelled. No model → rule engine.

**Say what is not verified.** The unverified phrase band, the "illustrative scenario" label on
flood extents, the honest model-status line. A crisis tool that overstates itself is worse than
one that admits a gap.

---

## Build and run

Requires **Android Studio** (bundled JDK 21) and a phone on **Android 12+**.

```bash
cd nongor-android

# Debug APK
./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk

# Release APK (~32 MB, arm64-v8a + armeabi-v7a)
./gradlew :app:assembleRelease

# The reasoning core, the phrasebook engine and the SMS codec are pure Kotlin
./gradlew :app:testDebugUnitTest    # 95 unit tests
```

On Windows, point Gradle at Android Studio's JDK:

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew :app:assembleDebug
```

Install and open it. There is no sign-in and no first-run download. **Turn aeroplane mode on
and use the whole app** — that is the intended demo.

To see the mesh work you need two phones with Nongor installed, Bluetooth and location on,
within about 100 m of each other.

---

## What is under the hood

```
nongor-android/app/src/main/
├── java/org/nongor/app/
│   ├── core/        pure-Kotlin engines — triage, GIS/Dijkstra, RAG, phrasebook,
│   │                conversation triage, SMS codec, mesh envelope, family crypto
│   ├── data/        prefs · districts · 9,525 shelters · region packs · repositories
│   ├── mesh/        Nearby Connections hub, Ed25519 identity, store-and-forward outbox
│   ├── inference/   optional on-device model runtime
│   ├── location/    fused location with a graceful district-centre fallback
│   └── ui/          home · translate · emergency · mesh · gis · firstaid · triage ·
│                    community · family · summary · chat · guide · onboarding · settings
└── assets/          phrasebook · road graphs · districts · shelters · first-aid packs
```

Everything in `core/` is free of Android imports, which is why it can be unit-tested on the
JVM — the disaster logic is verifiable without a device and without the model.

---

## Data and attribution

- **Roads & shelters** — © OpenStreetMap contributors (ODbL); road networks via Overpass,
  shelters via HOT-OSM `hotosm_bgd_education_facilities`.
- **Districts** — [nuhil/bangladesh-geocode](https://github.com/nuhil/bangladesh-geocode) (gov.bd-sourced).
- **Chakma, Marma & Garo phrases** — MELD: *A multilingual ethnic dataset of Chakma, Garo and
  Marma in Bengali script with English and standard Bengali translation*, Mahi, Khan, Anik &
  Mojumdar, Daffodil International University —
  [Mendeley Data](https://data.mendeley.com/datasets/dy5dyfygbp/4), **CC BY 4.0**.
- **Rohingya, Kokborok & Santali vocabulary** — GATITOS, from Google Research's
  [SMOL](https://huggingface.co/datasets/google/smol) collection of professional translations
  into low-resource languages, **CC BY 4.0**. Published in Latin script, and shown that way.
- **First-aid content** — grounded in WHO / IFRC / Red Cross guidance.
- **Emergency numbers** — official Government of Bangladesh short codes.
- **Icons** — Feather (MIT) and Material Symbols (Apache-2.0).
- Flood extents in the region packs are **illustrative scenarios**, not live flood data.
- AI assistance was used during development and is disclosed in our submission.

## Licence

MIT — see [LICENSE](LICENSE). Please fork it, and please correct our phrasebook.
