package amal.global.amal

import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File

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

        Promise
                .all<Unit>(sequenceOf<Promise<Unit>>(
                        reportReference.child("title").setValuePromise(reportDraft.title),
                        reportReference.child("authorDeviceToken").setValuePromise(reportDraft.localIdentifier),
                        reportReference.child("creationDate").setValuePromise(reportDraft.creationDate.time),
                        Promise.all<Unit>(reportDraft.images.map { uploadImage(it, reportReference.child("images").push()) }.asSequence()).map({ Unit })
                ))
                .flatMap {
                    reportReference.child("uploadComplete").setValuePromise(true)
                }
                .map {
                    return@map Report(
                            listOf(),
                            reportDraft.localIdentifier,
                            reportDraft.creationDate,
                            reportDraft.title,
                            reportDraft.assessorEmail
                    )
                }
                .then { report ->
                    promise.fulfill(report)
                }
                .catch { error ->
                    promise.reject(error)
                }
    }

    fun uploadImage(image: Image, reference: DatabaseReference): Promise<Unit> {

        val imageReference = imagesDirectory.child(reference.key)

        val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

        return Promise.all<Unit>(sequenceOf<Promise<Unit>>(
                imageReference.putFilePromise(Uri.fromFile(File(image.filePath)), metadata),
                reference.child("imageRef").setValuePromise(imageReference.path)
        )).map { Unit }
    }
}
