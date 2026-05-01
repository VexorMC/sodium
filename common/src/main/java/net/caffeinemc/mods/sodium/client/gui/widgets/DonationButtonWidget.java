package net.caffeinemc.mods.sodium.client.gui.widgets;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.Layout;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.util.Dim2i;

public class DonationButtonWidget {
    private static final int DONATE_BUTTON_WIDTH = 100;
    private static final int CLOSE_BUTTON_MARGIN = 3;

    private final FlatButtonWidget hideDonateButton;
    private final FlatButtonWidget donateButtonText;
    private boolean donateButtonEnabled;
    
    public DonationButtonWidget(VideoSettingsScreen parent, Runnable openDonationPage, Runnable hideDonationButton) {
        this.hideDonateButton = new FlatButtonWidget(new Dim2i(parent.getDimensions().x() - Layout.BUTTON_SHORT - Layout.INNER_MARGIN, parent.getDimensions().y(), Layout.BUTTON_SHORT, Layout.BUTTON_SHORT), new LiteralText("x"), hideDonationButton, true, false);
        this.donateButtonText = new FlatButtonWidget(new Dim2i(this.hideDonateButton.getX() - CLOSE_BUTTON_MARGIN - DONATE_BUTTON_WIDTH, parent.getDimensions().y(), DONATE_BUTTON_WIDTH, Layout.BUTTON_SHORT), new TranslatableText("sodium.options.buttons.donate"), openDonationPage, true, false);

        this.updateDisplay(parent, !SodiumClientMod.options().notifications.hasClearedDonationButton);
    }

    public void updateDisplay(VideoSettingsScreen parent, boolean enabled) {
        this.donateButtonEnabled = enabled;
        parent.setWidgetPresence(this.hideDonateButton, this.donateButtonEnabled);
        parent.setWidgetPresence(this.donateButtonText, this.donateButtonEnabled);
    }
    
    public int getWidth() {
        if (this.donateButtonEnabled) {
            return DONATE_BUTTON_WIDTH + Layout.BUTTON_SHORT + CLOSE_BUTTON_MARGIN + Layout.INNER_MARGIN * 2;
        } else {
            return 0;
        }
    }
}
