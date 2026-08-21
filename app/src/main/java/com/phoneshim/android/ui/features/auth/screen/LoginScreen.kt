package com.phoneshim.android.ui.features.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoneshim.android.R
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.ui.features.auth.component.GoogleSignInButton
import com.phoneshim.android.ui.features.auth.component.KakaoLoginButton
import com.phoneshim.android.ui.features.auth.viewmodel.LoginUiState
import com.phoneshim.android.ui.theme.PhoneShimDimens
import com.phoneshim.android.ui.theme.PhoneShimTheme
import com.phoneshim.android.ui.theme.PhoneShimType

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    noticeMessage: String? = null,
    onGoogleLogin: () -> Unit,
    onKakaoLogin: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = PhoneShimTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = PhoneShimDimens.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(
                PhoneShimDimens.spacing32,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(
                    48.dp,
                    Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.phoneshim_mascot),
                    contentDescription = null,
                    modifier = Modifier.size(width = 156.dp, height = 164.dp),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                ) {
                    Image(
                        painter = painterResource(R.drawable.phoneshim_wordmark),
                        contentDescription = null,
                        modifier = Modifier.size(width = 88.dp, height = 39.dp),
                    )
                    Text(
                        text = "잠시, 폰을 쉬게 하다",
                        color = PhoneShimTheme.colors.brandStrong,
                        style = PhoneShimType.KorCaption,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GoogleSignInButton(
                    onClick = onGoogleLogin,
                    enabled = uiState.canGoogleLogin && !uiState.isLoading,
                )
                KakaoLoginButton(
                    onClick = onKakaoLogin,
                    enabled = !uiState.isLoading,
                )
                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PhoneShimDimens.spacing12),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PhoneShimTheme.colors.brandStrong,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = when (uiState.selectedProvider) {
                                SocialProvider.GOOGLE -> stringResource(R.string.google_login_in_progress)
                                SocialProvider.KAKAO -> stringResource(R.string.kakao_login_in_progress)
                                null -> stringResource(R.string.social_login_in_progress)
                            },
                            color = PhoneShimTheme.colors.textSecondary,
                            style = PhoneShimType.KorCaption,
                        )
                    }
                }
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        color = PhoneShimTheme.colors.error,
                        style = PhoneShimType.KorLabel,
                        textAlign = TextAlign.Center,
                    )
                }
                if (uiState.errorMessage == null) {
                    noticeMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.fillMaxWidth(),
                            color = PhoneShimTheme.colors.textSecondary,
                            style = PhoneShimType.KorLabel,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            val consentPrefix = stringResource(R.string.login_consent_prefix)
            val privacyPolicyLabel = stringResource(R.string.privacy_policy_label)
            val consentSuffix = stringResource(R.string.login_consent_suffix)
            val privacyPolicyLinkColor = PhoneShimTheme.colors.brandStrong
            val consentText = buildAnnotatedString {
                append(consentPrefix)
                append(' ')
                withLink(
                    LinkAnnotation.Clickable(
                        tag = PRIVACY_POLICY_TAG,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = privacyPolicyLinkColor,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ),
                        linkInteractionListener = { onPrivacyPolicyClick() },
                    ),
                ) {
                    append(privacyPolicyLabel)
                }
                append(consentSuffix)
            }
            Text(
                text = consentText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PhoneShimDimens.spacing24),
                style = PhoneShimType.KorLabel.copy(
                    color = PhoneShimTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

private const val PRIVACY_POLICY_TAG = "privacy_policy"

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    PhoneShimTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onGoogleLogin = {},
            onKakaoLogin = {},
            onPrivacyPolicyClick = {},
        )
    }
}
