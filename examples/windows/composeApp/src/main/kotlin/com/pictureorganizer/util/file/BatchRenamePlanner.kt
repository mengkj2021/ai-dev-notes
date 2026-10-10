package com.pictureorganizer.util.file

import com.pictureorganizer.model.ImageListItem

object BatchRenamePlanner {
    data class Action(
        val id: String,
        val displayName: String,
        val targetFileName: String,
    )

    data class Collision(
        val id: String,
        val displayName: String,
        val targetFileName: String,
    )

    data class Plan(
        val renames: List<Action>,
        val collisions: List<Collision>,
    )

    fun plan(
        pattern: String,
        items: List<ImageListItem>,
    ): Plan {
        val claimedTargets = linkedSetOf<String>()
        val renames = mutableListOf<Action>()
        val collisions = mutableListOf<Collision>()

        for ((index, item) in items.withIndex()) {
            val displayName = fileNameOf(item)
            val target =
                RenamePatternApplier.applyForItem(
                    pattern = pattern,
                    item = item,
                    currentFileName = displayName,
                    sequence = index + 1,
                )
            if (target == displayName) continue
            if (target in claimedTargets) {
                collisions +=
                    Collision(
                        id = item.id,
                        displayName = displayName,
                        targetFileName = target,
                    )
            } else {
                claimedTargets += target
                renames +=
                    Action(
                        id = item.id,
                        displayName = displayName,
                        targetFileName = target,
                    )
            }
        }
        return Plan(renames = renames, collisions = collisions)
    }

    fun fileNameOf(item: ImageListItem): String {
        if (item.filePath.isNotEmpty()) return item.filePath.substringAfterLast('/')
        return item.description
    }
}
