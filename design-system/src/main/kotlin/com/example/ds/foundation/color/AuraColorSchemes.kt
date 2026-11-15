package com.example.ds.foundation.color

/**
 * Default Light and Dark semantic mappings for [AuraColors].
 *
 * Light follows a One UI-inspired cool-gray canvas ([AuraPalette.Neutral50]) with crisp
 * white surfaces ([AuraPalette.Neutral0]). Dark uses an OLED-friendly black canvas
 * ([AuraPalette.Neutral1000]) with stepped charcoal surfaces so future Glass layers stand out.
 */
object AuraColorSchemes {

    val Light: AuraColors = with(AuraPalette) {
        AuraColors(
            isDark = false,
            background = AuraColors.Background(
                primary = Neutral50,
                secondary = Neutral100,
            ),
            surface = AuraColors.Surface(
                default = Neutral0,
                raised = Neutral0,
                sunken = Neutral100,
                floating = Neutral0,
                inverse = Neutral900,
            ),
            content = AuraColors.Content(
                primary = Neutral900,
                secondary = Neutral600,
                tertiary = Neutral500,
                disabled = Neutral400,
                inverse = Neutral0,
            ),
            brand = AuraColors.Brand(
                primary = Blue600,
                onPrimary = Neutral0,
                primaryPressed = Blue700,
                subtle = Blue50,
                onSubtle = Blue800,
            ),
            border = AuraColors.Border(
                subtle = Neutral100,
                default = Neutral200,
                strong = Neutral400,
                focus = Blue500,
            ),
            status = AuraColors.Status(
                error = AuraColors.StatusRole(
                    solid = Red600,
                    onSolid = Neutral0,
                    subtle = Red50,
                    onSubtle = Red800,
                ),
                success = AuraColors.StatusRole(
                    solid = Green700,
                    onSolid = Neutral0,
                    subtle = Green50,
                    onSubtle = Green800,
                ),
                warning = AuraColors.StatusRole(
                    solid = Amber500,
                    onSolid = Neutral900,
                    subtle = Amber50,
                    onSubtle = Amber800,
                ),
                info = AuraColors.StatusRole(
                    solid = Cyan700,
                    onSolid = Neutral0,
                    subtle = Cyan50,
                    onSubtle = Cyan800,
                ),
            ),
            interaction = AuraColors.Interaction(
                pressedOverlay = Neutral1000.copy(alpha = 0.08f),
                selectedContainer = Blue50,
                onSelected = Blue700,
                disabledContainer = Neutral100,
                scrim = Neutral1000.copy(alpha = 0.40f),
            ),
        )
    }

    val Dark: AuraColors = with(AuraPalette) {
        AuraColors(
            isDark = true,
            background = AuraColors.Background(
                primary = Neutral1000,
                secondary = Neutral950,
            ),
            surface = AuraColors.Surface(
                default = Neutral900,
                raised = Neutral850,
                sunken = Neutral950,
                floating = Neutral800,
                inverse = Neutral50,
            ),
            content = AuraColors.Content(
                primary = Neutral50,
                secondary = Neutral300,
                tertiary = Neutral400,
                disabled = Neutral600,
                inverse = Neutral900,
            ),
            brand = AuraColors.Brand(
                primary = Blue400,
                onPrimary = Blue950,
                primaryPressed = Blue300,
                subtle = Blue900,
                onSubtle = Blue100,
            ),
            border = AuraColors.Border(
                subtle = Neutral800,
                default = Neutral700,
                strong = Neutral500,
                focus = Blue400,
            ),
            status = AuraColors.Status(
                error = AuraColors.StatusRole(
                    solid = Red400,
                    onSolid = Neutral950,
                    subtle = Red900,
                    onSubtle = Red100,
                ),
                success = AuraColors.StatusRole(
                    solid = Green400,
                    onSolid = Neutral950,
                    subtle = Green900,
                    onSubtle = Green100,
                ),
                warning = AuraColors.StatusRole(
                    solid = Amber400,
                    onSolid = Neutral950,
                    subtle = Amber900,
                    onSubtle = Amber100,
                ),
                info = AuraColors.StatusRole(
                    solid = Cyan400,
                    onSolid = Neutral950,
                    subtle = Cyan900,
                    onSubtle = Cyan100,
                ),
            ),
            interaction = AuraColors.Interaction(
                pressedOverlay = Neutral0.copy(alpha = 0.10f),
                selectedContainer = Blue900,
                onSelected = Blue200,
                disabledContainer = Neutral850,
                scrim = Neutral1000.copy(alpha = 0.65f),
            ),
        )
    }
}
