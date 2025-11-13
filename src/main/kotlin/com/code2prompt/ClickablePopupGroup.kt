package com.code2prompt

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware

/**
 * A custom action group that remains visible even when its actions are disabled.
 * When popup="true", clicking the group executes its first action.
 * This class ensures the group doesn't disappear from the context menu.
 */
class ClickablePopupGroup : DefaultActionGroup(), DumbAware {

    /**
     * By returning false, we explicitly tell the IDE *not* to hide this group
     * even if it has no visible child actions. This is the correct way to
     * ensure the group remains in the menu, where it will appear grayed out if disabled.
     */
    override fun hideIfNoVisibleChildren(): Boolean = false

    /**
     * Ensures that the update logic for this action group runs on a background thread,
     * preventing any potential UI freezes, especially during indexing.
     */
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}