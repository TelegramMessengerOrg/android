package org.telegram.messenger.exoplayer2.trackselection;

import org.telegram.messenger.exoplayer2.RendererConfiguration;
import org.telegram.messenger.exoplayer2.util.Util;

public final class TrackSelectorResult {
    public final Object info;
    public final int length;
    public final RendererConfiguration[] rendererConfigurations;
    public final TrackSelectionArray selections;

    public TrackSelectorResult(RendererConfiguration[] rendererConfigurations, TrackSelection[] selections, Object info) {
        this.rendererConfigurations = rendererConfigurations;
        this.selections = new TrackSelectionArray(selections);
        this.info = info;
        this.length = rendererConfigurations.length;
    }

    public boolean isRendererEnabled(int index) {
        return this.rendererConfigurations[index] != null;
    }

    public boolean isEquivalent(TrackSelectorResult other) {
        if (other == null || other.selections.length != this.selections.length) {
            return false;
        }
        for (int i = 0; i < this.selections.length; i++) {
            if (!isEquivalent(other, i)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEquivalent(TrackSelectorResult other, int index) {
        if (other != null && Util.areEqual(this.rendererConfigurations[index], other.rendererConfigurations[index]) && Util.areEqual(this.selections.get(index), other.selections.get(index))) {
            return true;
        }
        return false;
    }
}
