# The Nongor phrasebook

This is the file that decides whether a rescuer can talk to the person in front of them:
[`nongor-android/app/src/main/assets/phrasebook.json`](../nongor-android/app/src/main/assets/phrasebook.json).

**If you speak Chakma, Marma, Kokborok, Santali, Garo or Rohingya, you can improve this app
more than any programmer can.** This page explains how, and what we deliberately did not do.

---

## What we did not do

We did not invent translations.

It would have been easy to fill this file with plausible-looking lines for all six languages
and demo beautifully. We did not, because the sentences in here are things like *"are you
bleeding?"* and *"do not touch the wire, it can kill you"*, and a confident wrong answer to
either of those is worse than no answer.

So:

- **Bangla and English are authored content.** We wrote them and we stand behind them.
- **Chakma and Rohingya have a small seed word list**, marked `"v": "unverified"`, which the
  app displays with a **যাচাই হয়নি · unverified** band every single time it appears.
- **Marma, Kokborok, Santali and Garo have no written lines yet.** The app says so, in the
  language picker, before you pick them.

None of this stops the app working, because the conversation does not run on the text. It runs
on the pictogram and the tap-reply — see below.

---

## Why a missing translation is not a broken feature

Every phrase carries four things, and only the second is language-dependent:

1. **A pictogram**, chosen to be readable at arm's length.
2. A written line in the other person's language — *when we have a verified one.*
3. **A structured reply kind**: yes/no, a number 1–10, a 1–5 pain scale, a body part, a time
   bucket, a distance bucket.
4. **A described BdSL-style gesture**, in Bangla.

So the worst case is: the volunteer shows a pictogram of a bleeding arm, the other person taps
a big green **হ্যাঁ**, and the answer lands in the hand-over note as `bleeding: yes`. No shared
language was involved at any point. Adding a verified written line makes that exchange faster
and less ambiguous — it does not make it possible, because it already was.

---

## How to add or verify a line

### 1. Find the phrase

Each entry looks like this:

```json
{
  "id": "bleeding", "cat": "medical", "icon": "blood", "reply": "yesno",
  "en": "Are you bleeding?", "bn": "আপনার কি রক্তপাত হচ্ছে?",
  "tags": ["blood", "bleeding", "রক্ত"],
  "sign_bn": "আঙুল দিয়ে হাত বেয়ে নিচে গড়িয়ে পড়ার ইশারা করুন।"
}
```

### 2. Add your language under `t`

```json
  "t": {
    "ccp": { "beng": "…", "latn": "…", "v": "verified" }
  }
```

| Field | Meaning |
| --- | --- |
| `beng` | The line **written in Bengali script**. This is what the other person reads. Bengali script is deliberate: a Bangla-reading volunteer can also sound it out, which matters when the person opposite cannot read at all. |
| `latn` | Roman transliteration, shown small underneath. |
| `v` | `verified`, `community`, or `unverified` — see below. |

### 3. Set `v` honestly

| Value | Use it when | How the app shows it |
| --- | --- | --- |
| `verified` | A native speaker checked this exact sentence **in this exact context** — that it means what the Bangla means, and that it is not rude, alarming or ambiguous to someone in distress. | Shown plainly. |
| `community` | Contributed in good faith, not yet double-checked. | Shown with the unverified band. |
| `unverified` | Anything else, including everything we seeded. | Shown with the unverified band. |

Please do not mark something `verified` because it looks right. The band is not a failure
state; it is the app being honest, and honesty is cheap compared to the alternative.

### 4. Language codes

| Code | Language | Region |
| --- | --- | --- |
| `ccp` | Chakma · চাঙমা | Chittagong Hill Tracts |
| `mrh` | Marma · মারমা | Chittagong Hill Tracts |
| `trp` | Kokborok · ককবরক | Tripura communities |
| `sat` | Santali · সাঁওতালি | Rajshahi, Rangpur |
| `grt` | Garo · আ·চিক | Mymensingh |
| `rhg` | Rohingya · Ruáingga | Cox's Bazar |
| `bdsl` | Bangladeshi Sign Language | Deaf community |

To move a language from `"status": "empty"` to `"seed"`, change it in the `languages` block at
the top of the file. The coverage count shown in the picker is computed from the actual number
of written lines — you never update it by hand, and it cannot drift.

---

## Adding a whole new phrase

Add an object to `phrases`. Required: `id`, `cat`, `icon`, `reply`, `en`, `bn`.

- `cat` — one of the ids in the `categories` block.
- `icon` — a key from `phraseIcon()` in
  [`PhraseIcons.kt`](../nongor-android/app/src/main/java/org/nongor/app/ui/translate/PhraseIcons.kt).
  An unknown key falls back to a question mark rather than crashing, but please add a real one:
  the pictogram is the part that works in every language.
- `reply` — `yesno`, `ack`, `number`, `scale`, `bodypart`, `hours`, `distance`, `text`, `none`.
- `tags` — search terms in **both** English and Bangla. A volunteer types "blood" or "রক্ত" and
  must reach the same phrase.
- `sign_bn` — how to gesture it, in Bangla.

To put a phrase in the ten guided rescue questions, add its `id` to `triage_flow`. An id in
that list that does not exist is skipped rather than crashing — there is a test for it.

---

## Checking your change

```bash
python -c "import json;json.load(open('nongor-android/app/src/main/assets/phrasebook.json',encoding='utf-8'))"
cd nongor-android && ./gradlew :app:testDebugUnitTest --tests '*Phrasebook*'
```

The tests cover the parts that would fail quietly: that coverage counts real lines rather than
aspirations, that an unverified line is never reported as verified, that a phrase with no
translation still resolves, and that a bad id in `triage_flow` drops the step instead of taking
the screen down.

---

## Where the medical wording came from

The medical questions are phrased against WHO and IFRC first-aid guidance and kept to things a
non-medic can act on: presence or absence, location, and severity. Nongor does not ask anyone
to diagnose. If you are a clinician and a question is leading, ambiguous or unsafe, that is a
bug worth opening — the phrasing matters as much as the translation.
