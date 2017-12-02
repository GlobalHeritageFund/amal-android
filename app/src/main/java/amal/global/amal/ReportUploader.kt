package amal.global.amal

import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File
import java.net.URI

class ReportUploader (val reportDraft: ReportDraft) {

    val promise = Promise<Report>()

    val database = FirebaseDatabase.getInstance()

    val storage = FirebaseStorage.getInstance()

    val reportsDirectory: DatabaseReference by lazy {
        database.reference.child("reports")
    }

    val imagesDirectory: StorageReference by lazy {
        storage.reference.child("images")
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

        val imageReference = imagesDirectory.child(reference.key)

        val metadata = StorageMetadata.Builder()
                .setContentType("image/type")
                .build()

        return Promise.all<Unit>(sequenceOf<Promise<Unit>>(
                imageReference.putFilePromise(Uri.fromFile(File(image.filePath))),
                reference.child("imageRef").setValuePromise(imageReference.path)
        )).map { Unit }
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

fun StorageReference.putFilePromise(uri: Uri, metadata: StorageMetadata): Promise<Unit> {
    return Promise<Unit>({ fulfill, reject ->
        val uploadTask = this.putFile(uri, metadata)
        uploadTask.addOnCompleteListener({ task ->
            fulfill(Unit)
        })
        uploadTask.addOnFailureListener({ exception ->
            reject(Error(exception.message))
        })
    })
}