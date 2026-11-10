package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ClassEntity
import com.example.ui.theme.StatusAlpaBg
import com.example.ui.theme.StatusAlpaRed
import com.example.ui.theme.StatusAlpaText
import com.example.ui.theme.StatusHadirBg
import com.example.ui.theme.StatusHadirGreen
import com.example.ui.theme.StatusHadirText
import com.example.ui.theme.StatusIzinBg
import com.example.ui.theme.StatusIzinBlue
import com.example.ui.theme.StatusIzinText
import com.example.ui.theme.StatusSakitAmber
import com.example.ui.theme.StatusSakitBg
import com.example.ui.theme.StatusSakitText

@Composable
fun ClassSelectorHeader(
    classes: List<ClassEntity>,
    selectedClassId: Long?,
    onSelectClass: (Long) -> Unit,
    onManageClasses: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentClass = classes.find { it.id == selectedClassId } ?: classes.firstOrNull()

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .testTag("class_selector_dropdown")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Kelas",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "KELAS AKTIF",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentClass?.name ?: "Pilih / Tambah Kelas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Pilih Kelas",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                classes.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    fontWeight = if (item.id == selectedClassId) FontWeight.Bold else FontWeight.Normal
                                )
                                if (item.id == selectedClassId) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Terpilih",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelectClass(item.id)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "+ Kelola / Tambah Kelas",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        expanded = false
                        onManageClasses()
                    }
                )
            }
        }
    }
}

/**
 * 1-Tap Toggle Group for Attendance (Hadir / Izin / Sakit / Alpa).
 * Each option is easily tappable with minimum 48dp touch target and clear colors.
 */
@Composable
fun AttendanceToggleGroup(
    currentStatus: String?, // "H", "I", "S", "A" or null
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf(
            Triple("H", "Hadir", Pair(StatusHadirGreen, StatusHadirBg)),
            Triple("I", "Izin", Pair(StatusIzinBlue, StatusIzinBg)),
            Triple("S", "Sakit", Pair(StatusSakitAmber, StatusSakitBg)),
            Triple("A", "Alpa", Pair(StatusAlpaRed, StatusAlpaBg))
        )

        options.forEach { (code, label, colors) ->
            val isSelected = currentStatus == code
            val activeColor = colors.first
            val softBg = colors.second

            Surface(
                color = if (isSelected) activeColor else softBg.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(width = 44.dp, height = 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onStatusSelected(code) }
                    .testTag("attendance_btn_$code")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else activeColor
                    )
                }
            }
        }
    }
}

@Composable
fun StatCountPill(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
