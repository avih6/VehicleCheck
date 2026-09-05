package com.avih6.vehiclecheck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avih6.vehiclecheck.R

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.app_name_full),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "גרסה 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // MOT Information
        InfoSectionCard(
            title = "אודות מבחן הרישוי השנתי (טסט)",
            icon = Icons.Outlined.Verified
        ) {
            Text(
                text = "כל רכב בישראל מחויב במבחן רישוי שנתי (טסט) על מנת לנוע בכבישי הארץ. רכבים חדשים עד גיל 3 פטורים מטסט שנתי, רכבים בני 3 עד 19 שנים מחויבים בטסט פעם בשנה, ורכבים מעל גיל 19 מחויבים בטסט פעמיים בשנה ובאישור מוסך לרכב מיושן.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Safety Ratings
        InfoSectionCard(
            title = "רמת אבזור בטיחותי (0-8)",
            icon = Icons.Outlined.Shield
        ) {
            Text(
                text = "דירוג הבטיחות נקבע ע\"י משרד התחבורה ומבוסס על מערכות בטיחות אקטיביות המותקנות ברכב (כגון בלימה אוטונומית, שמירה על נתיב, התרעת שטח מת, בקרת שיוט אדפטיבית ועוד). ככל שהציון גבוה יותר, הרכב מצויד במערכות בטיחות מתקדמות יותר.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Emission Groups
        InfoSectionCard(
            title = "קבוצות זיהום אוויר (1-15)",
            icon = Icons.Outlined.Eco
        ) {
            Text(
                text = "קבוצות הזיהום נעות בין 1 ל-15, כאשר קבוצה 1 כוללת רכבים חשמליים מלאים ללא פליטות מזהמים, וקבוצה 15 כוללת רכבים בעלי פליטת מזהמים גבוהה. קבוצת הזיהום משפיעה על מס הקנייה ועל אגרת הרישוי.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Dedicated Disabled Permit App
        val context = androidx.compose.ui.platform.LocalContext.current
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Accessible,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "אפליקציית בדיקת תו נכה",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "אפליקציה ייעודית וקלת משקל לבדיקה ממוקדת של תווי חניית נכים בזמן אמת. אידיאלית לבדיקות שטח מהירות בחניונים ובדרכים.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { com.avih6.vehiclecheck.util.ExternalAppUtils.openDisabledPermitApp(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("פתיחה / הורדה של האפליקציה")
                }
            }
        }

        // Legal Disclaimer
        InfoSectionCard(
            title = stringResource(R.string.disclaimer_title),
            icon = Icons.Outlined.Gavel
        ) {
            Text(
                text = stringResource(R.string.disclaimer_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}