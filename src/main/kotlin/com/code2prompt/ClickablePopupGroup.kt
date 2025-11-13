package com.code2prompt

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware

/**
 * A custom action group that remains visible even when its actions are disabled.
 * When popup="true", clicking the group executes its first action.
 * This class ensures the group doesn't disappear from the context menu,
 * which can happen in modern IDE versions if a popup group is disabled.
 */
class ClickablePopupGroup : DefaultActionGroup(), DumbAware {

    /**
     * By returning true, we explicitly tell the IDE to keep this group visible
     * even if it has no enabled actions. This is the modern way to prevent
     * action groups from being automatically hidden to reduce UI clutter.
     */
    override fun isAlwaysVisible(): Boolean = true

    override fun update(e: AnActionEvent) {
        // Run the default update logic first. This will correctly set the group's
        // enabled/disabled state based on its children.
        super.update(e)
        // We still set isVisible to true for robustness, ensuring that UI rendering logic
        // across different versions and themes respects our intention to show the group.
        e.presentation.isVisible = true
    }
}