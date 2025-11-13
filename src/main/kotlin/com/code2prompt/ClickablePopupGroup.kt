package com.code2prompt

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware

/**
 * A custom action group that remains visible even when its actions are disabled.
 * When popup="true", clicking the group executes its first action.
 * This class ensures the group doesn't disappear from the context menu.
 */
class ClickablePopupGroup : DefaultActionGroup(), DumbAware {

    /**
     * This is the classic method to prevent the IDE from hiding a group.
     * By calling super.update() first, we allow the group to be correctly
     * disabled if its children are disabled. Then, we force it to remain
     * visible, ensuring it shows up as a grayed-out, unclickable item
     * instead of disappearing entirely.
     */
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = true
    }

    /**
     * Ensures that the update logic for this action group runs on a background thread,
     * preventing any potential UI freezes and adhering to modern plugin best practices.
     */
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}