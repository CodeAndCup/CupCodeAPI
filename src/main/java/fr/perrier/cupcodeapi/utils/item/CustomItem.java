package fr.perrier.cupcodeapi.utils.item;

import fr.perrier.cupcodeapi.utils.*;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a custom item in the plugin with associated behavior and properties.
 * This class allows creating and managing custom items that can be used throughout the plugin,
 * with optional interaction handling and custom display names.
 * 
 * <p>Custom items are automatically registered in a static list upon creation and can be
 * retrieved later using {@link #getCustomItem(ItemStack)}.</p>
 * 
 * <p>Features include:</p>
 * <ul>
 *   <li>Custom display names with automatic formatting</li>
 *   <li>Optional interaction handling through Consumer callbacks</li>
 *   <li>Support for both Material-based and ItemStack-based custom items</li>
 *   <li>Automatic registration and management of all custom items</li>
 * </ul>
 * 
 * @see CustomItemEvent
 */
@Getter
public class CustomItem {

    @Getter
    private static final List<CustomItem> customItems = new ArrayList<>();

    private final Material material;
    private final ItemStack itemStack;
    private final String name;
    private final boolean interactItem;
    private final Consumer<CustomItemEvent> callable;
    
    /**
     * Creates a new custom item with the specified material and name.
     * The item will be interactable by default.
     *
     * @param material The material of the custom item
     * @param name The display name of the custom item (will be automatically formatted)
     */
    public CustomItem(Material material, String name) {
        this.material = material;
        this.name = name;
        this.interactItem = true;
        this.callable = null;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    /**
     * Creates a new custom item with the specified material, name, and interactability.
     *
     * @param material The material of the custom item
     * @param name The display name of the custom item (will be automatically formatted)
     * @param interactItem Whether the item can be interacted with
     */
    public CustomItem(Material material, String name, boolean interactItem) {
        this.material = material;
        this.name = name;
        this.interactItem = interactItem;
        this.callable = null;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    /**
     * Creates a new interactable custom item with the specified material, name, and interaction handler.
     *
     * @param material The material of the custom item
     * @param name The display name of the custom item (will be automatically formatted)
     * @param event The consumer that will handle interaction events for this item
     */
    public CustomItem(Material material, String name, Consumer<CustomItemEvent> event) {
        this.material = material;
        this.name = name;
        this.interactItem = true;
        this.callable = event;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    /**
     * Creates a new custom item using an existing ItemStack with a custom name and interaction handler.
     *
     * @param is The ItemStack to use as the base for this custom item
     * @param name The display name of the custom item (will be automatically formatted)
     * @param event The consumer that will handle interaction events for this item
     */
    public CustomItem(ItemStack is, String name, Consumer<CustomItemEvent> event) {
        this.material = null;
        this.name = name;
        this.interactItem = true;
        this.callable = event;
        itemStack = is;
        if(!customItems.contains(this)) customItems.add(this);
    }

    /**
     * Creates a new custom item with the specified material, name, interactability, and interaction handler.
     * This is the most flexible constructor allowing full control over all properties.
     *
     * @param material The material of the custom item
     * @param name The display name of the custom item (will be automatically formatted)
     * @param interactItem Whether the item can be interacted with
     * @param event The consumer that will handle interaction events for this item
     */
    public CustomItem(Material material, String name, boolean interactItem, Consumer<CustomItemEvent> event) {
        this.material = material;
        this.name = name;
        this.interactItem = interactItem;
        this.callable = event;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    /**
     * Converts this custom item to a Bukkit ItemStack with proper formatting.
     * The display name will be automatically formatted with color codes.
     *
     * @return A new ItemStack representing this custom item
     */
    public ItemStack toItemStack() {
        if(itemStack != null) {
            return new ItemBuilder(itemStack).setName("&f&l" + name).toItemStack();
        }
        return new ItemBuilder(material).setName("&f&l" + name ).toItemStack();
    }

    /**
     * Retrieves a CustomItem instance that matches the given ItemStack.
     * The comparison is done using ItemStack's isSimilar() method.
     *
     * @param itemStack The ItemStack to find a matching CustomItem for
     * @return The matching CustomItem, or null if no match is found
     */
    public static CustomItem getCustomItem(ItemStack itemStack) {
        return getCustomItems().stream().filter(c -> itemStack.isSimilar(c.toItemStack())).findFirst().orElse(null);
    }
}
