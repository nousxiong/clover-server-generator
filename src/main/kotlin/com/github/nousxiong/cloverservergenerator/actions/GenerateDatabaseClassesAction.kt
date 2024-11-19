package com.github.nousxiong.cloverservergenerator.actions

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.github.nousxiong.cloverservergenerator.settings.PluginSettings
import com.github.nousxiong.cloverservergenerator.dialogs.GenerateDatabaseClassesDialog
import java.nio.charset.StandardCharsets

class GenerateDatabaseClassesAction : AnAction() {
    companion object {
        const val BEGIN_MARKER = "/* BEGIN GENERATED CODE */"
        const val END_MARKER = "/* END GENERATED CODE */"
    }

    /**
     * 获取项目的 src/main/kotlin 目录。如果不存在，则创建它。
     */
    private fun getFixedBaseDirectory(project: Project): PsiDirectory? {
        // 获取项目的 base path
        val basePath = project.basePath ?: return null
        println("project base path: $basePath")
        val root = PluginSettings.getInstance().state.root

        // 构建绝对路径
        val rootPath = "$basePath/$root"

        // 获取 VirtualFile 对象
        val baseVirtualFile = VfsUtil.findFileByIoFile(java.io.File(rootPath), true) ?: run {
//            // 如果目录不存在，尝试创建
//            val srcMainVirtualFile = VfsUtil.findFileByIoFile(java.io.File("$basePath/src/main"), true)
//            if (srcMainVirtualFile != null && srcMainVirtualFile.isDirectory) {
//                srcMainVirtualFile.createChildDirectory(this, "kotlin")
//            } else {
//                null
//            }
            null
        } ?: return null

        // 获取 PsiDirectory 对象
        return PsiManager.getInstance(project).findDirectory(baseVirtualFile)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.project ?: return
//        val dataContext = event.dataContext
//        val directory: PsiDirectory? = com.intellij.openapi.actionSystem.LangDataKeys.PSI_ELEMENT.getData(dataContext) as? PsiDirectory
//            ?: com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE.getData(dataContext)?.containingDirectory
        val directory = getFixedBaseDirectory(project)
        if (directory == null) {
            Messages.showErrorDialog(project, "Cannot determine target directory.", "Error")
            return
        }

        // 显示自定义对话框，获取用户输入
        val dialog = GenerateDatabaseClassesDialog(project)
        if (dialog.showAndGet()) {
            val functionName = dialog.functionName
            if (functionName.isBlank()) {
                Messages.showErrorDialog(project, "Function name cannot be empty.", "Error")
                return
            }

            val poClassName = dialog.customPOClassName
            val daoClassName = dialog.customDAOClassName
            val serviceClassName = dialog.customServiceClassName
            val mapperName = dialog.customMapperXmlName
            val poPackageName = dialog.poPackageName
            val daoPackageName = dialog.daoPackageName
            val servicePackageName = dialog.servicePackageName
            val mapperPackageName = dialog.mapperPackageName

            // 确认生成
            val filesToGenerate = mutableListOf<String>()
            if (dialog.generatePO) filesToGenerate.add("PO Class: $poClassName in package $poPackageName")
            if (dialog.generateDAO) filesToGenerate.add("DAO Class: $daoClassName in package $daoPackageName")
            if (dialog.generateService) filesToGenerate.add("Service Class: $serviceClassName in package service")
            if (dialog.generateMapper) filesToGenerate.add("Mapper: $mapperName in package mapper")

            val confirm = Messages.showYesNoDialog(
                project,
                "You are about to generate the following files:\n" +
                        filesToGenerate.joinToString("\n") +
                        "\n\nDo you want to proceed?",
                "Confirm Generation",
                Messages.getQuestionIcon()
            )
            if (confirm != Messages.YES) {
                return
            }

            generateClasses(
                project,
                directory,
                poClassName,
                daoClassName,
                serviceClassName,
                mapperName,
                poPackageName,
                daoPackageName,
                servicePackageName,
                mapperPackageName,
                dialog.generatePO,
                dialog.generateDAO,
                dialog.generateService,
                dialog.generateMapper,
                dialog.selectedPOTemplate,
                dialog.selectedDAOTemplate,
                dialog.selectedServiceTemplate,
                dialog.selectedMapperTemplate
            )
        }
    }

    private fun generateClasses(
        project: Project,
        baseDirectory: PsiDirectory,
        poClassName: String,
        daoClassName: String,
        serviceClassName: String,
        mapperName: String,
        poPackageName: String,
        daoPackageName: String,
        servicePackageName: String,
        mapperPackageName: String,
        generatePO: Boolean,
        generateDAO: Boolean,
        generateService: Boolean,
        generateMapper: Boolean,
        selectedPOTemplate: String,
        selectedDAOTemplate: String,
        selectedServiceTemplate: String,
        selectedMapperTemplate: String
    ) {
        // 加载模板
        val templateManager = FileTemplateManager.getInstance(project)

        val poTemplate = if (generatePO) {
            templateManager.getJ2eeTemplate(selectedPOTemplate)/* ?: run {
                Messages.showErrorDialog(project, "PO template '$selectedPOTemplate' not found.", "Error")
                return
            }*/
        } else null

        val daoTemplate = if (generateDAO) {
            templateManager.getJ2eeTemplate(selectedDAOTemplate)/* ?: run {
                Messages.showErrorDialog(project, "DAO template '$selectedDAOTemplate' not found.", "Error")
                return
            }*/
        } else null

        val serviceTemplate = if (generateService) {
            templateManager.getJ2eeTemplate(selectedServiceTemplate)/* ?: run {
                Messages.showErrorDialog(project, "Service template '$selectedServiceTemplate' not found.", "Error")
                return
            }*/
        } else null

        val mapperTemplate = if (generateMapper) {
            templateManager.getJ2eeTemplate(selectedMapperTemplate)/* ?: run {
                Messages.showErrorDialog(project, "Mapper XML template '$selectedMapperTemplate' not found.", "Error")
                return
            }*/
        } else null

        val properties = mutableMapOf<String, Any>(
            "CLASS_NAME" to poClassName.replace("PO", ""), // 用于 DAO 和 Service 的类名
            "PO_PACKAGE_NAME" to poPackageName, // PO包名
            "DAO_PACKAGE_NAME" to daoPackageName, // DAO包名
            "SERVICE_PACKAGE_NAME" to servicePackageName, // Service包名
            "PO_CLASS_NAME" to poClassName,
            "DAO_CLASS_NAME" to daoClassName,
            "SERVICE_CLASS_NAME" to serviceClassName,
            "MAPPER_NAME" to mapperName,
            // 添加更多属性，如需要
        )

        // 使用 WriteCommandAction 以确保修改在写命令中执行
        WriteCommandAction.runWriteCommandAction(project) {
            // 生成 PO 类
            if (generatePO && poTemplate != null) {
                val poDirectory = getOrCreateDirectory(baseDirectory, "kotlin", poPackageName)
                createOrUpdateFile(project, poDirectory, "$poClassName.kt", poTemplate, properties)
            }

            // 生成 DAO 类
            if (generateDAO && daoTemplate != null) {
                val daoDirectory = getOrCreateDirectory(baseDirectory, "kotlin", daoPackageName)
                createOrUpdateFile(project, daoDirectory, "$daoClassName.kt", daoTemplate, properties)
            }

            // 生成 Service 类
            if (generateService && serviceTemplate != null) {
                val serviceDirectory = getOrCreateDirectory(baseDirectory, "kotlin", servicePackageName)
                createOrUpdateFile(project, serviceDirectory, "$serviceClassName.kt", serviceTemplate, properties)
            }

            // 生成 Mapper XML 文件
            if (generateMapper && mapperTemplate != null) {
                val mapperDirectory = getOrCreateDirectory(baseDirectory, "resources", mapperPackageName)
                createOrUpdateFile(project, mapperDirectory, "$mapperName.xml", mapperTemplate, properties)
            }
        }
    }

    private fun getOrCreateDirectory(baseDirectory: PsiDirectory, rootPath: String, packageName: String): PsiDirectory {
        val rootSegments = rootPath.split("/").filter { it.isNotBlank() }
        var currentDir = baseDirectory
        for (segment in rootSegments) {
            currentDir = currentDir.findSubdirectory(segment) ?: currentDir.createSubdirectory(segment)
        }

        if (packageName.isBlank()) {
            return currentDir
        }

        val packageSegments = packageName.split(".")
        for (segment in packageSegments) {
            currentDir = currentDir.findSubdirectory(segment) ?: currentDir.createSubdirectory(segment)
        }
        return currentDir
    }

    private fun createOrUpdateFile(
        project: Project,
        directory: PsiDirectory,
        fileName: String,
        template: com.intellij.ide.fileTemplates.FileTemplate,
        properties: Map<String, Any>
    ) {
        val existingFile = directory.findFile(fileName)
        if (existingFile != null) {
            // 更新已有文件的生成部分
            try {
                val content = String(existingFile.virtualFile.contentsToByteArray(), StandardCharsets.UTF_8)
                val newGeneratedContent = template.getText(properties)

                val updatedContent = updateGeneratedSection(content, newGeneratedContent)
                existingFile.virtualFile.setBinaryContent(updatedContent.toByteArray(StandardCharsets.UTF_8))
            } catch (e: Exception) {
                Messages.showErrorDialog(project, "Error updating file $fileName: ${e.message}", "Error")
            }
        } else {
            // 创建新文件
            try {
                val newFile = directory.createFile(fileName)
                val content = template.getText(properties)
                newFile.virtualFile.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
            } catch (e: Exception) {
                Messages.showErrorDialog(project, "Error creating file $fileName: ${e.message}", "Error")
            }
        }
    }

    // 获取生成区域得内容
    private fun getGeneratedContent(content: String): String {
        val beginIndex = content.indexOf(BEGIN_MARKER)
        val endIndex = content.indexOf(END_MARKER)

        return if (beginIndex != -1 && endIndex != -1 && endIndex > beginIndex) {
            content.substring(beginIndex + BEGIN_MARKER.length, endIndex)
        } else {
            content
        }
    }

    private fun updateGeneratedSection(originalContent: String, newContent: String): String {
        val generatedContent = getGeneratedContent(newContent)

        val beginIndex = originalContent.indexOf(BEGIN_MARKER)
        val endIndex = originalContent.indexOf(END_MARKER)

        return if (beginIndex != -1 && endIndex != -1 && endIndex > beginIndex) {
            val before = originalContent.substring(0, beginIndex + BEGIN_MARKER.length)
            val after = originalContent.substring(endIndex)
//            val generatedSection = "\n$generatedContent\n"
            before + generatedContent + after
        } else {
            // 如果标记不存在，则简单地追加生成内容
            originalContent + "\n$BEGIN_MARKER\n$generatedContent\n$END_MARKER\n"
        }
    }
}
