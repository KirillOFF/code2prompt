package com.code2prompt

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

/**
 * A custom action group that remains visible even when its actions are disabled.
 * When popup="true", clicking the group executes its first action.
 * This prevents the group from disappearing from the context menu when conditions for its actions are not met.
 */
class ClickablePopupGroup : DefaultActionGroup() {

    override fun update(e: AnActionEvent) {
        // First, run the default update logic. This will set the presentation
        // based on the child actions (e.g., disable the group if the first action is disabled).
        super.update(e)

        // Now, force the group to be visible, regardless of its enabled state.
        // This ensures it doesn't disappear from the menu, but will still appear
        // grayed out if its actions are not available.
        e.presentation.isVisible = true
    }
}