package org.nongor.app

import android.app.Application
import org.nongor.app.data.AppPrefs
import org.nongor.app.data.ChatRepository
import org.nongor.app.data.CommunityRepository
import org.nongor.app.data.FamilyRepository
import org.nongor.app.data.SosRepository
import org.nongor.app.data.download.HfDownloadRepository
import org.nongor.app.inference.EngineHolder
import org.nongor.app.location.LocationProvider
import org.nongor.app.mesh.MeshHub
import java.io.File

class NongorApplication : Application() {

    lateinit var chatRepository: ChatRepository
        private set
    lateinit var downloadRepository: HfDownloadRepository
        private set
    lateinit var engineHolder: EngineHolder
        private set
    lateinit var prefs: AppPrefs
        private set
    lateinit var sosRepository: SosRepository
        private set
    lateinit var communityRepository: CommunityRepository
        private set
    lateinit var familyRepository: FamilyRepository
        private set
    lateinit var locationProvider: LocationProvider
        private set
    lateinit var meshHub: MeshHub
        private set

    /**
     * A handover note composed on the translation screen, waiting to be sent as an SOS.
     *
     * Held in memory rather than passed as a navigation argument because the note is
     * multi-line free text and round-tripping it through a route string would mangle it.
     * Consumed exactly once by the mesh screen, then cleared.
     */
    var pendingSosDraft: String? = null

    override fun onCreate() {
        super.onCreate()
        chatRepository = ChatRepository(this)
        downloadRepository = HfDownloadRepository()
        engineHolder = EngineHolder(this)
        prefs = AppPrefs(this)
        sosRepository = SosRepository(File(filesDir, "sos_store.json"))
        communityRepository = CommunityRepository(File(filesDir, "community_store.json"))
        familyRepository = FamilyRepository(File(filesDir, "family_store.json"))
        locationProvider = LocationProvider(this)
        meshHub = MeshHub(this, sosRepository, communityRepository, familyRepository,
            locationProvider, prefs)
    }
}
