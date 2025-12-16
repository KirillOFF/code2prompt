package com.code2prompt

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class TestAction : DumbAwareAction("Test Action - Always Visible") {

    companion object {
        private val LOG = Logger.getInstance(TestAction::class.java)
    }

    override fun update(e: AnActionEvent) {
        LOG.info("TestAction.update() called - should always be visible")
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("TestAction.actionPerformed() called")
        Messages.showInfoMessage("Test action works!", "Debug")
    }
}