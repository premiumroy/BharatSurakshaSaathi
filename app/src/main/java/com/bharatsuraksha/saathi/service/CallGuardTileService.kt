package com.bharatsuraksha.saathi.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.bharatsuraksha.saathi.R

/**
 * A Quick Settings tile: one more way for the USER to start Call Guard during a
 * call. Tapping it never starts anything by itself unless the tile is tapped.
 */
@RequiresApi(Build.VERSION_CODES.N)
class CallGuardTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    override fun onClick() {
        super.onClick()
        if (CallGuardService.isRunning()) {
            CallGuardService.stop(this)
        } else {
            CallGuardService.start(this)
        }
        refresh()
    }

    private fun refresh() {
        val tile: Tile = qsTile ?: return
        val running = CallGuardService.isRunning()
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.agent_guard_tile_label)
        tile.subtitle = getString(
            if (running) R.string.agent_guard_on else R.string.agent_guard_off
        )
        tile.updateTile()
    }
}
