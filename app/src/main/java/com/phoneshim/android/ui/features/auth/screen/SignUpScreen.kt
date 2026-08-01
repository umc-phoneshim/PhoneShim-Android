package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.phoneshim.android.ui.common.PrimaryButton
import com.phoneshim.android.ui.common.SecondaryButton
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize().padding(PhoneShimDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing16),
    ) {
        Text(text = "회원가입", style = PhoneShimType.KorH1)
        Text(text = "시연용 계정을 만들어 목표 설정을 시작해요.", style = PhoneShimType.KorBodyM)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton(
            text = "가입하고 시작하기",
            onClick = onSignUpSuccess,
            enabled = name.isNotBlank() && email.contains('@'),
        )
        SecondaryButton(text = "뒤로", onClick = onNavigateBack)
    }
}
