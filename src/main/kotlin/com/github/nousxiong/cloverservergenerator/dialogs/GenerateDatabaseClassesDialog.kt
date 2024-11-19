package com.github.nousxiong.cloverservergenerator.dialogs

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.github.nousxiong.cloverservergenerator.settings.PluginSettings
import java.util.*
import javax.swing.*

class GenerateDatabaseClassesDialog(private val project: Project) : DialogWrapper(project) {

    var functionName: String = ""
    var poClassName: String = ""
    var daoClassName: String = ""
    var serviceClassName: String = ""
    var mapperName: String = ""
    var poPackageName: String = ""
    var daoPackageName: String = ""
    var servicePackageName: String = ""
    var mapperPackageName: String = ""

    // 用户选择是否生成每种文件类型
    var generatePO: Boolean = true
    var generateDAO: Boolean = true
    var generateService: Boolean = true
    var generateMapper: Boolean = true

    // 用户选择的模板名称
    var selectedPOTemplate: String = ""
    var selectedDAOTemplate: String = ""
    var selectedServiceTemplate: String = ""
    var selectedMapperTemplate: String = ""

    // 用户自定义的最终名称
    var customPOClassName: String = ""
    var customDAOClassName: String = ""
    var customServiceClassName: String = ""
    var customMapperXmlName: String = ""

    private lateinit var functionNameField: JTextField
    private lateinit var poCheckBox: JCheckBox
    private lateinit var poTemplateComboBox: JComboBox<String>
    private lateinit var daoCheckBox: JCheckBox
    private lateinit var daoTemplateComboBox: JComboBox<String>
    private lateinit var serviceCheckBox: JCheckBox
    private lateinit var serviceTemplateComboBox: JComboBox<String>
    private lateinit var mapperCheckBox: JCheckBox
    private lateinit var mapperTemplateComboBox: JComboBox<String>

    private lateinit var poPackageField: JTextField
    private lateinit var daoPackageLabel: JLabel
    private lateinit var servicePackageLabel: JLabel
    private lateinit var mapperPackageLabel: JLabel

    private lateinit var poClassField: JTextField
    private lateinit var daoClassField: JTextField
    private lateinit var serviceClassField: JTextField
    private lateinit var mapperField: JTextField

    init {
        title = "Generate Kotlin Classes"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            val settings = PluginSettings.getInstance()
            row("Function Name:") {
                functionNameField = textField().validationOnInput {
                    if (it.text.isBlank()) error("Function name cannot be empty")
                    else null
                }
                .component.apply {
                    // 添加文档监听器，当文本变化时更新预览
                    document.addDocumentListener(object : javax.swing.event.DocumentListener {
                        override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updateNames()
                        override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updateNames()
                        override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updateNames()
                    })
                }
            }
            separator()
            row {
                label("Select File Types to generate:")
            }
            row {
                poCheckBox = checkBox("PO class").component.apply { isSelected = true }
                poTemplateComboBox = comboBox(getTemplates("PO")).component.apply {
                    isEnabled = poCheckBox.isSelected
                }
            }
            row {
                daoCheckBox = checkBox("DAO class").component.apply { isSelected = true }
                daoTemplateComboBox = comboBox(getTemplates("DAO")).component.apply {
                    isEnabled = daoCheckBox.isSelected
                }
            }
            row {
                serviceCheckBox = checkBox("Service class").component.apply { isSelected = true }
                serviceTemplateComboBox = comboBox(getTemplates("Service")).component.apply {
                    isEnabled = serviceCheckBox.isSelected
                }
            }
            row {
                mapperCheckBox = checkBox("Mapper XML").component.apply { isSelected = true }
                mapperTemplateComboBox = comboBox(getTemplates("Mapper")).component.apply {
                    isEnabled = mapperCheckBox.isSelected
                }
            }
            separator()
            // Custem names
            if (poCheckBox.isSelected) {
                row("PO Class Name:") {
                    poClassField = textField().component.apply {
                        document.addDocumentListener(object : javax.swing.event.DocumentListener {
                            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(poClassField)
                            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(poClassField)
                            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(poClassField)
                        })
                    }
                }
            }
            if (daoCheckBox.isSelected) {
                row("DAO Class Name:") {
                    daoClassField = textField().component.apply {
                        document.addDocumentListener(object : javax.swing.event.DocumentListener {
                            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(daoClassField)
                            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(daoClassField)
                            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(daoClassField)
                        })
                    }
                }
            }
            if (serviceCheckBox.isSelected) {
                row("Service Class Name:") {
                    serviceClassField = textField().component.apply {
                        document.addDocumentListener(object : javax.swing.event.DocumentListener {
                            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(serviceClassField)
                            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(serviceClassField)
                            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(serviceClassField)
                        })
                    }
                }
            }
            if (mapperCheckBox.isSelected) {
                row("Mapper XML File Name:") {
                    mapperField = textField().component.apply {
                        document.addDocumentListener(object : javax.swing.event.DocumentListener {
                            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(mapperField)
                            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(mapperField)
                            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updateDiyName(mapperField)
                        })
                    }
                }
            }

            separator()
            // Generated Names Preview:
            poPackageName = settings.state.poPkgRoot
            if (poCheckBox.isSelected) {
                row("PO Package:") {
                    poPackageField = textField().component.apply {
                        document.addDocumentListener(object : javax.swing.event.DocumentListener {
                            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = updatePackageName(poPackageField)
                            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = updatePackageName(poPackageField)
                            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = updatePackageName(poPackageField)
                        })
                    }
//                    label("Class: ").component
//                    label(poClassField.text).component
                }
            }
            daoPackageName = settings.state.daoPkgRoot
            if (daoCheckBox.isSelected) {
                row("DAO Package:") {
                    daoPackageLabel = label(daoPackageName).component
//                    label("Class: ").component
//                    label(daoClassField.text).component
                }
            }
            servicePackageName = settings.state.servicePkgRoot
            if (serviceCheckBox.isSelected) {
                row("Service Class:") {
                    servicePackageLabel = label(servicePackageName).component
//                    label("Class: ").component
//                    label(serviceClassField.text).component
                }
            }
            mapperPackageName = settings.state.mapperRoot
            if (mapperCheckBox.isSelected) {
                row("Mapper XML:") {
                    mapperPackageLabel = label(mapperPackageName).component
//                    label("File: ").component
//                    label(mapperXmlField.text).component
                }
            }
        }.also {
            // 添加监听器以启用/禁用模板选择下拉菜单和名称字段
            poCheckBox.addActionListener {
                poTemplateComboBox.isEnabled = poCheckBox.isSelected
                poClassField.isEnabled = poCheckBox.isSelected
            }
            daoCheckBox.addActionListener {
                daoTemplateComboBox.isEnabled = daoCheckBox.isSelected
                daoClassField.isEnabled = daoCheckBox.isSelected
            }
            serviceCheckBox.addActionListener {
                serviceTemplateComboBox.isEnabled = serviceCheckBox.isSelected
                serviceClassField.isEnabled = serviceCheckBox.isSelected
            }
            mapperCheckBox.addActionListener {
                mapperTemplateComboBox.isEnabled = mapperCheckBox.isSelected
                mapperField.isEnabled = mapperCheckBox.isSelected
            }
        }
    }

    override fun doOKAction() {
        functionName = functionNameField.text.trim()
        generatePO = poCheckBox.isSelected
        generateDAO = daoCheckBox.isSelected
        generateService = serviceCheckBox.isSelected
        generateMapper = mapperCheckBox.isSelected

        selectedPOTemplate = if (generatePO) poTemplateComboBox.selectedItem as String else ""
        selectedDAOTemplate = if (generateDAO) daoTemplateComboBox.selectedItem as String else ""
        selectedServiceTemplate = if (generateService) serviceTemplateComboBox.selectedItem as String else ""
        selectedMapperTemplate = if (generateMapper) mapperTemplateComboBox.selectedItem as String else ""

        customPOClassName = if (generatePO) poClassField.text.trim() else ""
        customDAOClassName = if (generateDAO) daoClassField.text.trim() else ""
        customServiceClassName = if (generateService) serviceClassField.text.trim() else ""
        customMapperXmlName = if (generateMapper) mapperField.text.trim() else ""

        super.doOKAction()
    }

    private fun updatePackageName(textField: JTextField) {
        val settings = PluginSettings.getInstance()
        val input = textField.text.trim()
        if (textField === poPackageField) {
            poPackageName = settings.state.poPkgRoot + "." + toPackageName(input)
        }
    }

    private fun updateDiyName(textField: JTextField) {
        val input = textField.text.trim()
        if (textField === poClassField) {
            poClassName = input
        }
        if (textField === daoClassField) {
            daoClassName = input
        }
        if (textField === serviceClassField) {
            serviceClassName = input
        }
        if (textField === mapperField) {
            mapperName = input
        }
    }

    private fun updateNames() {
        val settings = PluginSettings.getInstance()
        val input = functionNameField.text.trim()
        if (input.isNotBlank()) {
            poClassName = "${toCamelCase(input)}PO"
            daoClassName = "${toCamelCase(input)}DAO"
            serviceClassName = "${toCamelCase(input)}Service"
            mapperName = toSnakeCase(input)

            poPackageName = settings.state.poPkgRoot + "." + toPackageName(input)
            daoPackageName = settings.state.daoPkgRoot + "." + toPackageName(input)
            servicePackageName = settings.state.servicePkgRoot
            mapperPackageName = settings.state.mapperRoot

            SwingUtilities.invokeLater {
                if (poCheckBox.isSelected) {
                    poClassField.text = poClassName
                    poPackageField.text = poPackageName
                }

                if (daoCheckBox.isSelected) {
                    daoClassField.text = daoClassName
                    daoPackageLabel.text = daoPackageName
                }

                if (serviceCheckBox.isSelected) {
                    serviceClassField.text = serviceClassName
                    servicePackageLabel.text = servicePackageName
                }

                if (mapperCheckBox.isSelected) {
                    mapperField.text = mapperName
                    mapperPackageLabel.text = mapperPackageName
                }
            }
        } else {
            poClassName = ""
            daoClassName = ""
            serviceClassName = ""
            mapperName = ""

            poPackageName = settings.state.poPkgRoot
            daoPackageName = settings.state.daoPkgRoot
            servicePackageName = settings.state.servicePkgRoot
            mapperPackageName = settings.state.mapperRoot

            SwingUtilities.invokeLater {
                if (poCheckBox.isSelected) {
                    poClassField.text = ""
                    poPackageField.text = poPackageName
                }

                if (daoCheckBox.isSelected) {
                    daoClassField.text = ""
                    daoPackageLabel.text = daoPackageName
                }

                if (serviceCheckBox.isSelected) {
                    serviceClassField.text = ""
                    servicePackageLabel.text = servicePackageName
                }

                if (mapperCheckBox.isSelected) {
                    mapperField.text = ""
                    mapperPackageLabel.text = mapperPackageName
                }
            }
        }
    }

    // 命名转换工具方法
    private fun toCamelCase(input: String): String {
        return input.split(" ").joinToString("") { s ->
            s.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private fun toSnakeCase(input: String): String {
        return input.trim().split("\\s+".toRegex()).joinToString("_") { it.lowercase(Locale.getDefault()) }
    }

    private fun toPackageName(input: String): String {
        return input.trim().split("\\s+".toRegex()).joinToString("") { it.lowercase(Locale.getDefault()) }
    }

    // 获取指定类型的模板列表
    private fun getTemplates(type: String): List<String> {
        val templateManager = FileTemplateManager.getInstance(project)
        println(templateManager.allJ2eeTemplates.map { "${it.name}.${it.extension}" })
        // 过滤Clover模板
        return templateManager.allJ2eeTemplates.filter {
            it.name.startsWith("Clover_${type}")
        }.map {
            "${it.name}.${it.extension}"
        }
    }
}
