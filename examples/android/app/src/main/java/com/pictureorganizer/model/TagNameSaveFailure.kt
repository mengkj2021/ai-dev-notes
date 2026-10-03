package com.pictureorganizer.model

enum class TagNameSaveFailure {
    Empty,
    Reserved,
    Duplicate,
}

fun classifyTagNameSaveFailure(
    trimmedName: String,
    nameTakenByOther: Boolean,
): TagNameSaveFailure? =
    when {
        trimmedName.isEmpty() -> TagNameSaveFailure.Empty
        ImageListItem.isReservedStatusName(trimmedName) -> TagNameSaveFailure.Reserved
        nameTakenByOther -> TagNameSaveFailure.Duplicate
        else -> null
    }

fun classifyTemplateNameSaveFailure(
    trimmedName: String,
    nameTakenByOther: Boolean,
): TagNameSaveFailure? =
    when {
        trimmedName.isEmpty() -> TagNameSaveFailure.Empty
        nameTakenByOther -> TagNameSaveFailure.Duplicate
        else -> null
    }
