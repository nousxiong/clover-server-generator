package com.github.nousxiong.cloverservergenerator

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

class MyFileTemplateGroupFactory : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("Kotlin Templates", null)

        // 列出模板
        val poTemplates = listOf(
            "Clover_PO_Default.kt",
            "Clover_PO_List.kt",
        )
        val daoTemplates = listOf(
            "Clover_DAO_Default.kt",
        )
        val serviceTemplates = listOf(
            "Clover_Service_Default.kt",
        )
        val mapperTemplates = listOf(
            "Clover_Mapper_Default.xml",
        )

        // 注册模板
        poTemplates.forEach {
            group.addTemplate(FileTemplateDescriptor(it))
        }
        daoTemplates.forEach {
            group.addTemplate(FileTemplateDescriptor(it))
        }
        serviceTemplates.forEach {
            group.addTemplate(FileTemplateDescriptor(it))
        }
        mapperTemplates.forEach {
            group.addTemplate(FileTemplateDescriptor(it))
        }

        return group
    }
}
