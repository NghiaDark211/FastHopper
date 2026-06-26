package com.nghiadark.fasthopper.listeners;

import com.nghiadark.fasthopper.FastHopper;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class HopperListener implements Listener {

    private final FastHopper plugin;

    public HopperListener(FastHopper plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle hopper moving items to another inventory (push)
     * and hopper pulling items from another inventory (pull).
     *
     * The vanilla event fires for 1 item at a time.
     * We schedule additional transfers on the next tick to move extra items
     * based on the configured transfer-amount.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        int transferAmount = plugin.getTransferAmount();

        // If transfer amount is 1 (vanilla), do nothing extra
        if (transferAmount <= 1) {
            return;
        }

        Inventory source = event.getSource();
        Inventory destination = event.getDestination();

        // Check if a hopper is involved (either source or destination holder is a Hopper)
        boolean isHopperSource = source.getHolder() instanceof Hopper;
        boolean isHopperDest = destination.getHolder() instanceof Hopper;

        if (!isHopperSource && !isHopperDest) {
            return;
        }

        // The event already handles 1 item transfer, so we need to move (transferAmount - 1) more
        int extraItems = transferAmount - 1;
        ItemStack movedItem = event.getItem().clone();

        // Schedule additional transfers on the next tick to avoid modifying inventories during the event
        new BukkitRunnable() {
            @Override
            public void run() {
                transferExtraItems(source, destination, movedItem, extraItems);
            }
        }.runTask(plugin);
    }

    /**
     * Transfer extra items from source to destination.
     * This method respects inventory space and item availability.
     */
    private void transferExtraItems(Inventory source, Inventory destination, ItemStack referenceItem, int amount) {
        int transferred = 0;

        for (int i = 0; i < source.getSize() && transferred < amount; i++) {
            ItemStack sourceItem = source.getItem(i);

            if (sourceItem == null || sourceItem.getType().isAir()) {
                continue;
            }

            // Calculate how many we can transfer from this slot
            int canTransfer = Math.min(sourceItem.getAmount(), amount - transferred);

            // Create the item to transfer
            ItemStack toTransfer = sourceItem.clone();
            toTransfer.setAmount(canTransfer);

            // Try to add to destination
            var leftover = destination.addItem(toTransfer);

            // Calculate how many actually were added
            int notAdded = 0;
            for (ItemStack left : leftover.values()) {
                notAdded += left.getAmount();
            }

            int actuallyTransferred = canTransfer - notAdded;

            if (actuallyTransferred > 0) {
                // Remove from source
                if (sourceItem.getAmount() <= actuallyTransferred) {
                    source.setItem(i, null);
                } else {
                    sourceItem.setAmount(sourceItem.getAmount() - actuallyTransferred);
                }
                transferred += actuallyTransferred;
            }

            // If destination is full, stop
            if (notAdded > 0) {
                break;
            }
        }
    }
}
