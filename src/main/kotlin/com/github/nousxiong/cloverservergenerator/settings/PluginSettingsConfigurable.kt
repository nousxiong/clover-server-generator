package com.github.nousxiong.cloverservergenerator.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.BoxLayout
import javax.swing.Box
import javax.swing.BorderFactory
import org.jetbrains.annotations.Nls

class PluginSettingsConfigurable : Configurable {

    private var rootPanel: JPanel? = null
    private lateinit var poRootField: JTextField
    private lateinit var daoRootField: JTextField
    private lateinit var serviceRootField: JTextField
    private lateinit var mapperRootField: JTextField

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Clover Server Generator"
    }

    override fun createComponent(): JComponent {
        if (rootPanel == null) {
            rootPanel = JPanel()
            rootPanel?.layout = BoxLayout(rootPanel, BoxLayout.Y_AXIS)
            rootPanel?.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

            // PO Root Directory
            val poPanel = JPanel()
            poPanel.layout = BoxLayout(poPanel, BoxLayout.X_AXIS)
            poPanel.add(JLabel("PO Root Directory: "))
            poRootField = JTextField(30)
            poPanel.add(poRootField)
            rootPanel?.add(poPanel)
            rootPanel?.add(Box.createVerticalStrut(10))

            // DAO Root Directory
            val daoPanel = JPanel()
            daoPanel.layout = BoxLayout(daoPanel, BoxLayout.X_AXIS)
            daoPanel.add(JLabel("DAO Root Directory: "))
            daoRootField = JTextField(30)
            daoPanel.add(daoRootField)
            rootPanel?.add(daoPanel)
            rootPanel?.add(Box.createVerticalStrut(10))

            // Service Root Directory
            val servicePanel = JPanel()
            servicePanel.layout = BoxLayout(servicePanel, BoxLayout.X_AXIS)
            servicePanel.add(JLabel("Service Root Directory: "))
            serviceRootField = JTextField(30)
            servicePanel.add(serviceRootField)
            rootPanel?.add(servicePanel)
            rootPanel?.add(Box.createVerticalStrut(10))

            // Mapper Root Directory
            val mapperPanel = JPanel()
            mapperPanel.layout = BoxLayout(mapperPanel, BoxLayout.X_AXIS)
            mapperPanel.add(JLabel("Mapper Root Directory: "))
            mapperRootField = JTextField(30)
            mapperPanel.add(mapperRootField)
            rootPanel?.add(mapperPanel)
            rootPanel?.add(Box.createVerticalStrut(10))
        }
        return rootPanel!!
    }

    override fun isModified(): Boolean {
        val settings = PluginSettings.getInstance().state
        return poRootField.text != settings.poPkgRoot ||
                daoRootField.text != settings.daoPkgRoot ||
                serviceRootField.text != settings.servicePkgRoot ||
                mapperRootField.text != settings.mapperRoot
    }

    override fun apply() {
        val settings = PluginSettings.getInstance().state
        settings.poPkgRoot = poRootField.text.trim()
        settings.daoPkgRoot = daoRootField.text.trim()
        settings.servicePkgRoot = serviceRootField.text.trim()
        settings.mapperRoot = mapperRootField.text.trim()
    }

    override fun reset() {
        val settings = PluginSettings.getInstance().state
        poRootField.text = settings.poPkgRoot
        daoRootField.text = settings.daoPkgRoot
        serviceRootField.text = settings.servicePkgRoot
        mapperRootField.text = settings.mapperRoot
    }

    override fun disposeUIResources() {
        rootPanel = null
    }
}
