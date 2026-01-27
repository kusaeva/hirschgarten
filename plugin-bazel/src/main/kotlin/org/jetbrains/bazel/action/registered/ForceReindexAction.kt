package org.jetbrains.bazel.action.registered

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.util.indexing.ScanningIterators
import com.intellij.util.indexing.ScanningParameters
import com.intellij.util.indexing.UnindexedFilesScanner
import com.intellij.util.indexing.diagnostic.ScanningType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.CompletableDeferred
import org.jetbrains.bazel.action.SuspendableAction
import org.jetbrains.bazel.config.BazelPluginBundle
import org.jetbrains.bazel.workspace.bazelProjectDirectoriesEntity
import com.intellij.util.indexing.FileBasedIndexTumbler
import com.intellij.util.indexing.CorruptionMarker
import org.jetbrains.bazel.sync.scope.SecondPhaseSync
import org.jetbrains.bazel.sync.task.ProjectSyncTask

class ForceReindexAction : SuspendableAction({ BazelPluginBundle.message("force.reindex.action.text") }) {
  override suspend fun actionPerformed(project: Project, e: AnActionEvent) {
      val project = e.getProject()
      if (project == null) return
      val scanningParameters = CompletableDeferred<ScanningIterators>(
        ScanningIterators(
          "Force re-scanning",
          null,
          null,
          ScanningType.FULL_FORCED,
        ),
      )
      val task = UnindexedFilesScanner(
        project,
        false,
        false,
        null,
        null,
        null,
        false,
        scanningParameters,
      )
      task.queue()
      ProjectSyncTask(project).sync(syncScope = SecondPhaseSync, buildProject = false)
    }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(project: Project, e: AnActionEvent) {
    e.presentation.isEnabled = project.bazelProjectDirectoriesEntity() != null
  }
}
