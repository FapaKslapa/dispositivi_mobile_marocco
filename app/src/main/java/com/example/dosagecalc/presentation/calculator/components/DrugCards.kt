package com.example.dosagecalc.presentation.calculator.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.DrugCategory
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.LocalElevation
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.label

private data class CategoryColors(
    val container: Color,
    val on: Color,
)

@Composable
private fun DrugCategory.colors(): CategoryColors {
    val cs = MaterialTheme.colorScheme
    return when (this) {
        DrugCategory.ONCOLOGY -> CategoryColors(cs.errorContainer, cs.onErrorContainer)
        DrugCategory.INFECTIOUS -> CategoryColors(cs.tertiaryContainer, cs.onTertiaryContainer)
        DrugCategory.PEDIATRICS -> CategoryColors(cs.secondaryContainer, cs.onSecondaryContainer)
        DrugCategory.DERMATOLOGY -> CategoryColors(cs.primaryContainer, cs.onPrimaryContainer)
        DrugCategory.OTHER -> CategoryColors(cs.surfaceVariant, cs.onSurfaceVariant)
    }
}

@Composable
fun DrugSelectionCard(
    drug: Drug,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "drug_card_scale",
    )
    val avatarColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "avatar_color",
    )
    val cs = MaterialTheme.colorScheme
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current
    val elev = LocalElevation.current
    val catColors = drug.category.colors()
    val isCustom = onEditClick != null || onDeleteClick != null

    Card(
        modifier =
            Modifier
                .width(268.dp)
                .wrapContentHeight()
                .scale(scale)
                .clip(shapes.card)
                .clickable { onClick() },
        shape = shapes.card,
        colors =
            CardDefaults.cardColors(
                containerColor = if (isSelected) cs.primaryContainer else cs.surfaceVariant,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isSelected) elev.level3 else elev.level1,
            ),
        border =
            BorderStroke(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) cs.primary else cs.outlineVariant.copy(alpha = 0.35f),
            ),
    ) {
        Column(modifier = Modifier.padding(sp.base)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(
                                if (isSelected) avatarColor else catColors.container,
                                shapes.field,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = drug.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                        color = if (isSelected) cs.onPrimary else catColors.on,
                    )
                }
                Spacer(modifier = Modifier.width(sp.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) cs.onPrimaryContainer else cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${drug.unitDose} ${drug.unit} · ${drug.formulaType.label()}",
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            if (isSelected) {
                                cs.onPrimaryContainer.copy(alpha = 0.75f)
                            } else {
                                cs.onSurfaceVariant
                            },
                    )
                }
                Spacer(modifier = Modifier.width(sp.xs))
                Surface(
                    shape = shapes.chip,
                    color = catColors.container.copy(alpha = if (isSelected) 0.4f else 0.8f),
                ) {
                    Text(
                        text =
                            drug.category.label
                                .take(4)
                                .uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        color = if (isSelected) cs.primary else catColors.on,
                    )
                }
            }

            Spacer(modifier = Modifier.height(sp.md))

            Text(
                text = drug.indication,
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isSelected) {
                        cs.onPrimaryContainer.copy(alpha = 0.85f)
                    } else {
                        cs.onSurfaceVariant
                    },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(36.dp),
            )

            Spacer(modifier = Modifier.height(sp.sm))
            HorizontalDivider(
                color =
                    if (isSelected) {
                        cs.primary.copy(alpha = 0.2f)
                    } else {
                        cs.outlineVariant.copy(alpha = 0.4f)
                    },
                thickness = 0.5.dp,
            )
            Spacer(modifier = Modifier.height(sp.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isCustom) Arrangement.spacedBy(sp.xs) else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DrugActionChip(
                    icon = Icons.Rounded.Info,
                    label = "Info",
                    onClick = onInfoClick,
                    isSelected = isSelected,
                    modifier = if (isCustom) Modifier.weight(1f) else Modifier,
                )
                if (onEditClick != null) {
                    DrugActionChip(
                        icon = Icons.Default.Edit,
                        label = "Modifica",
                        onClick = onEditClick,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (onDeleteClick != null) {
                    Surface(
                        shape = shapes.chip,
                        color = cs.errorContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, cs.error.copy(alpha = 0.45f)),
                    ) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Elimina",
                                tint = cs.error,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrugActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    val shapes = LocalDosageShapes.current
    val sp = MaterialTheme.spacing
    Surface(
        onClick = onClick,
        shape = shapes.chip,
        color = if (isSelected) cs.primary.copy(alpha = 0.12f) else cs.surface,
        border = BorderStroke(1.dp, if (isSelected) cs.primary.copy(alpha = 0.4f) else cs.outlineVariant),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = sp.sm, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) cs.primary else cs.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) cs.primary else cs.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DrugPreviewCard(drug: Drug) {
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.cardLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Column(modifier = Modifier.padding(sp.xl)) {
            Text(
                text = "Dettagli Clinici",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(sp.base))

            Surface(
                shape = shapes.tile,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(sp.base), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(sp.base))
                    Column {
                        Text(
                            text = "Dosaggio Base",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${drug.unitDose} ${drug.unit} (${drug.formulaType.label()})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            if (drug.alert.isNotBlank()) {
                Spacer(modifier = Modifier.height(sp.md))
                Surface(
                    shape = shapes.tile,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(sp.base), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(sp.base))
                        Column {
                            Text(
                                text = "Attenzione",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = drug.alert,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrugCategoryFilterRow(
    selectedCategory: DrugCategory?,
    onCategorySelected: (DrugCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalDosageShapes.current
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    val chipColors =
        FilterChipDefaults.filterChipColors(
            selectedContainerColor = cs.primary,
            selectedLabelColor = cs.onPrimary,
        )
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = sp.lg),
        horizontalArrangement = Arrangement.spacedBy(sp.sm),
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Tutti") },
                shape = shapes.chip,
                colors = chipColors,
            )
        }
        items(DrugCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.label) },
                shape = shapes.chip,
                colors = chipColors,
            )
        }
    }
}
