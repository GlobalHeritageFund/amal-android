package amal.global.amal

class ReportUploader (val reportDraft: ReportDraft) {

    val promise = Promise<Report>()

    fun upload() {
        val firebase = Firebase()
        Promise.all<Unit>(sequenceOf<Promise<Unit>>(
                firebase.set("title", reportDraft.title),
                firebase.set("authorDeviceToken", reportDraft.localIdentifier),
                firebase.set("creationDate", reportDraft.creationDate.time)
        ))
    }
}

class Firebase {
    fun set(key: String, value: Any): Promise<Unit> {
        return Promise<Unit>({ fulfill, reject ->
            reject(Error("placeholder"))
        })
    }
}