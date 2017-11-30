package amal.global.amal

class ReportUploader (val reportDraft: ReportDraft) {

    val promise = Promise<Report>()

    fun upload() {
        val firebase = Firebase()
        Promise.all<Unit>(sequenceOf<Promise<Unit>>(
                firebase.set("title", reportDraft.title),
                firebase.set("authorDeviceToken", reportDraft.localIdentifier),
                firebase.set("creationDate", reportDraft.creationDate.time)
//                Promise.all<Unit>(reportDraft.images.map { uploadImage(it, "key") }).map({ Unit() })
        )).map {
            return@map Report(
                    listOf(),
                    reportDraft.localIdentifier,
                    reportDraft.creationDate,
                    reportDraft.title,
                    reportDraft.assessorEmail
                    )
        }.then { report ->
            promise.fulfill(report)
        }.catch { error ->
            promise.reject(error)
        }
    }

    fun uploadImage(image: Image, name: String): Promise<Unit> {
        //upload image
        //set firebase ref
        return Promise<Unit>({ fulfill, reject ->  reject(Error("placeholder")) })
    }
}

class Firebase {
    fun set(key: String, value: Any): Promise<Unit> {
        return Promise<Unit>({ fulfill, reject ->
            reject(Error("placeholder"))
        })
    }
}