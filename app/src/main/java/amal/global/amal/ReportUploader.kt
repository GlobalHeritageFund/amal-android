package amal.global.amal

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ReportUploader (val reportDraft: ReportDraft) {

    val promise = Promise<Report>()

    val database = FirebaseDatabase.getInstance()

    val reportsDirectory: DatabaseReference by lazy {
        database.reference.child("reports")
    }

    fun upload() {
        val reportReference = reportsDirectory.push()

        Promise.all<Unit>(sequenceOf<Promise<Unit>>(
                reportReference.child("title").setValuePromise(reportDraft.title),
                reportReference.child("authorDeviceToken").setValuePromise(reportDraft.localIdentifier),
                reportReference.child("creationDate").setValuePromise(reportDraft.creationDate.time),
                Promise.all<Unit>(reportDraft.images.map { uploadImage(it, reportReference) }.asSequence()).map({ Unit })
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

    fun uploadImage(image: Image, reference: DatabaseReference): Promise<Unit> {
        //upload image
        //set firebase ref
        return Promise<Unit>({ fulfill, reject ->  reject(Error("placeholder")) })
    }
}

fun DatabaseReference.setValuePromise(value: Any): Promise<Unit> {
    return Promise<Unit>({ fulfill, reject ->
        this.setValue(value, { databaseError, databaseReference ->
            if (databaseError != null) {
                reject(Error(databaseError.message))
            } else {
                fulfill(Unit)
            }
        })
    })
}