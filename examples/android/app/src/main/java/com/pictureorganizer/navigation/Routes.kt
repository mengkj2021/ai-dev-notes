package com.pictureorganizer.navigation

import android.net.Uri
import com.pictureorganizer.model.TagFilterCriteria

object Routes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val IMPORT_IMAGES = "import-images"
    const val IMAGE_DETAIL = "image-detail/{imageId}"
    const val SETTINGS = "settings"
    const val TAG_MANAGE = "tag-manage"
    const val FILTER =
        "filter?tags={tags}&untagged={untagged}&q={q}&sort={sort}" +
            "&dateFrom={dateFrom}&dateTo={dateTo}&importFrom={importFrom}&importTo={importTo}"
    const val TUTORIAL = "tutorial?fromSettings={fromSettings}"
    const val RENAME_TEMPLATE_MANAGE = "rename-template-manage"
    const val RENAME_TEMPLATE_EDIT = "rename-template-edit"
    const val RENAME_TEMPLATE_EDIT_ID = "rename-template-edit/{templateId}"
    const val DEFAULT_TAGS = "default-tags"
    const val EXPORT_ZIP = "export-zip"
    const val EXPORT_MANAGE = "export-manage"
    const val OSS_LICENSES = "oss-licenses"

    fun imageDetail(imageId: String): String = "image-detail/$imageId"

    fun filter(criteria: TagFilterCriteria = TagFilterCriteria()): String {
        val tags = Uri.encode(criteria.encodeTagsParam())
        val q = Uri.encode(criteria.nameContains)
        val dateFrom = criteria.encodeEpochDayParam(criteria.dateTakenFromEpochDay)
        val dateTo = criteria.encodeEpochDayParam(criteria.dateTakenToEpochDay)
        val importFrom = criteria.encodeEpochDayParam(criteria.importedAtFromEpochDay)
        val importTo = criteria.encodeEpochDayParam(criteria.importedAtToEpochDay)
        return "filter?tags=$tags&untagged=${criteria.includeUntagged}" +
            "&q=$q&sort=${criteria.sort.name}&dateFrom=$dateFrom&dateTo=$dateTo" +
            "&importFrom=$importFrom&importTo=$importTo"
    }

    fun tutorial(fromSettings: Boolean = false): String = "tutorial?fromSettings=$fromSettings"

    fun renameTemplateEdit(templateId: String? = null): String =
        if (templateId.isNullOrBlank()) {
            RENAME_TEMPLATE_EDIT
        } else {
            "rename-template-edit/$templateId"
        }
}
