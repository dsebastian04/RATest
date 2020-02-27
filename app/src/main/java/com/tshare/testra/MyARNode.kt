package com.tshare.testra

import android.content.Context
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture

class MyARNode(context: Context, modelId: Int) : AnchorNode() {

    var image: AugmentedImage? = null
        set(value) {
            if (!modelRenderableCompletableFuture.isDone) {
                CompletableFuture.allOf(modelRenderableCompletableFuture)
                    .thenAccept { field = value }
            }

            anchor = image?.createAnchor(image!!.centerPose)
            val node = Node()
            val pose = Pose.makeTranslation(0.0f, 0.0f, 0.0f)
            node.setParent(this)
            node.localPosition = Vector3(pose.tx(), pose.ty(), pose.tz())
            node.localRotation = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
            node.renderable = modelRenderableCompletableFuture.getNow(null)

        }
    private var modelRenderableCompletableFuture: CompletableFuture<ModelRenderable> = ModelRenderable.builder()
        .setRegistryId("my_model")
        .setSource(context, modelId)
        .build()


}