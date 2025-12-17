package com.code2prompt

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.util.containers.ContainerUtil
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

class FilesToPromptAction : DumbAwareAction() {

    companion object {
        private val LOG = Logger.getInstance(FilesToPromptAction::class.java)
        private val NOTIFICATION_GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup("FilesToPromptGroup")

        private val FILE_WITH_CONTENT: Condition<VirtualFile?> = Condition { f ->
            f != null && !f.fileType.isBinary
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("FilesToPromptAction.actionPerformed() called")

        val project = e.project
        if (project == null || project.isDefault) {
            LOG.warn("Action cancelled: project is null or default")
            return
        }

        // Get the selected files or folders
        val dataContext = e.dataContext
        val selectedFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext)

        LOG.debug("Selected files: ${selectedFiles?.size ?: 0}")

        if (selectedFiles.isNullOrEmpty()) {
            LOG.debug("No files selected, returning")
            return
        }
        val ignorePatterns = loadIgnorePatterns(project)

        // Collect the paths and contents
        val fileDataList = mutableListOf<String>()

        for (file in selectedFiles) {
            collectFileData(file, fileDataList, ignorePatterns)
        }

        // Format the data
        val result = fileDataList.joinToString("\n\n")

        // Copy to clipboard
        CopyPasteManager.getInstance().setContents(StringSelection(result))

        // Notify the user
NOTIFICATION_GROUP.createNotification(
            "Files data copied to clipboard.",
            NotificationType.INFORMATION
        ).notify(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        LOG.debug("FilesToPromptAction.update() called")

        val project = e.project
        LOG.debug("Project: $project, isDefault: ${project?.isDefault}")

        if (project == null || project.isDefault) {
            LOG.debug("Hiding action: project is null or default")
            e.presentation.isEnabledAndVisible = false
            return
        }

        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val file: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val files: Array<VirtualFile>? = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        LOG.debug("Editor: $editor, File: $file, Files array size: ${files?.size}")

        // Simplified logic for debugging - show if any files are selected
        val hasAnyFiles = file != null || (files != null && files.isNotEmpty())
        LOG.debug("hasAnyFiles: $hasAnyFiles")

        // Show action if any files are selected (simplified for debugging)
        e.presentation.isEnabledAndVisible = hasAnyFiles

        LOG.debug("Action visibility set to: ${e.presentation.isEnabledAndVisible}")
    }

    val MAX_FILE_SIZE: Long = (5 * 1024 * 1024 // 5 MB
            ).toLong()

    private fun loadIgnorePatterns(project: Project): List<Pattern> {
        val copyIgnoreFile = findCopyIgnoreFile(project)
        if (copyIgnoreFile != null && copyIgnoreFile.exists()) {
            return copyIgnoreFile.readLines().mapNotNull { line ->
                if (line.isNotBlank() && !line.startsWith("#")) {
                    val regexPattern = line.trim()
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".")
                    Pattern.compile("^$regexPattern\$")
                } else {
                    null
                }
            }
        }
        return emptyList()
    }

    private fun findCopyIgnoreFile(project: Project): File? {
        val baseDir = project.basePath?.let { File(it) } ?: return null
        val gitIgnoreFile = File(baseDir, ".gitignore")
        return if (gitIgnoreFile.exists()) {
            File(baseDir, ".topromptignore")
        } else {
            File(baseDir, ".topromptignore")
        }
    }

    private fun collectFileData(file: VirtualFile, fileDataList: MutableList<String>, ignorePatterns: List<Pattern>) {
        if (isIgnored(file, ignorePatterns)) {
            return
        }

        if (file.isDirectory) {
            file.children.forEach { child ->
                // Recursively collect data from all children, including META-INF
                collectFileData(child, fileDataList, ignorePatterns)
            }
        } else {
            try {
                if (file.getLength() > MAX_FILE_SIZE) {
                    // Skip large files
                    return
                }
                if (!file.fileType.isBinary) {
                    val path = file.path
                    val content = String(file.contentsToByteArray(), StandardCharsets.UTF_8)
                    val fileData = formatFileData(path, content)
                    fileDataList.add(fileData)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun isIgnored(file: VirtualFile, ignorePatterns: List<Pattern>): Boolean {
        val relativePath = file.path.replace("\\", "/")
        return ignorePatterns.any { it.matcher(relativePath).matches() }
    }

    private fun formatFileData(path: String, content: String): String {
        return "### File: " + path + "\n```" + getFileExtension(path) + "\n" + content + "\n```\n"
    }

    private fun getFileExtension(path: String): String {
        val lastIndex: Int = path.lastIndexOf('.')
        if (lastIndex != -1 && lastIndex != path.length - 1) {
            return path.substring(lastIndex + 1)
        }
        return ""
    }
}
