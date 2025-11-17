package hnau.echospeak.projector.themes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.FullScreen
import hnau.common.app.projector.uikit.TopBar
import hnau.common.app.projector.uikit.TopBarAction
import hnau.common.app.projector.uikit.TopBarTitle
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.echospeak.model.themes.ThemesListModel
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.themes
import hnau.echospeak.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import org.jetbrains.compose.resources.stringResource

class ThemesListProjector(
    private val model: ThemesListModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth
    }

    @Composable
    fun Content() {
        FullScreen(
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    TopBarTitle { Text(stringResource(Res.string.themes)) }
                    TopBarAction(
                        onClick = model.selectAll.collectAsState().value,
                    ) {
                        Icon(Icons.Default.SelectAll)
                    }
                    TopBarAction(
                        onClick = model.selectNone.collectAsState().value,
                    ) {
                        Icon(Icons.Default.Deselect)
                    }
                }
            },
        ) { contentPadding ->
            Overcompose(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                bottom = { paddingValues ->
                    Box(
                        modifier = Modifier.padding(paddingValues),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        model
                            .launch
                            .collectAsState()
                            .value
                            .NullableStateContent(
                                modifier = Modifier.padding(
                                    top = Dimens.separation,
                                    start = Dimens.separation,
                                    end = Dimens.horizontalDisplayPadding,
                                    bottom = Dimens.verticalDisplayPadding,
                                ),
                                transitionSpec = TransitionSpec.vertical(),
                            ) { launch ->
                                FloatingActionButton(
                                    onClick = launch,
                                ) {
                                    Icon(Icons.Default.PlayCircle)
                                }
                            }
                    }
                }
            ) { contentPadding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                ) {
                    items(
                        items = model.themes,
                        key = { theme -> theme.id.id },
                    ) { theme ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = theme.switchIsSelected),
                            headlineContent = { Text(theme.id.id) },
                            leadingContent = {
                                Checkbox(
                                    checked = theme.isSelected.collectAsState().value,
                                    onCheckedChange = null,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}