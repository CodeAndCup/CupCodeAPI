package fr.perrier.cupcodeapi.utils;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;


public class ItemBuilder {
    private final ItemStack is;

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m The material to create the ItemBuilder with.
     */
    public ItemBuilder(Material m) {
        this(m, 1);
    }

    /**
     * Create a new ItemBuilder over an existing itemstack.
     *
     * @param is The itemstack to create the ItemBuilder over.
     */
    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m      The material of the item.
     * @param amount The amount of the item.
     */
    public ItemBuilder(Material m, int amount) {
        is = new ItemStack(m, amount);
    }

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m          The material of the item.
     * @param amount     The amount of the item.
     * @param durability The durability of the item.
     */
    public ItemBuilder(Material m, int amount, byte durability) {
        is = new ItemStack(m, amount, durability);
    }

    /**
     * Clone the ItemBuilder into a new one.
     *
     * @return The cloned instance.
     */
    public ItemBuilder clone() {
        return new ItemBuilder(is);
    }

    /**
     * Change the durability of the item.
     *
     * @param dur The durability to set it to.
     */
    public ItemBuilder setDurability(short dur) {
        is.setDurability(dur);
        return this;
    }

    /**
     * Change the durability of the item.
     *
     * @param dur The durability to set it to.
     */
    public ItemBuilder setDurability(int dur) {
        setDurability((short) dur);
        return this;
    }

    /**
     * Change the texture of an item
     *
     * @param hash the head hash (base64)
     */
    public ItemBuilder setTexture(String hash) {
        if (!(this.is.getItemMeta() instanceof SkullMeta)) {
            return this;
        }

        SkullMeta skullMeta = (SkullMeta) this.is.getItemMeta();

        try {
            // Méthode moderne pour 1.21+
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            PropertyMap propertyMap = profile.getProperties();
            propertyMap.put("textures", new Property("textures", hash));

            // Utiliser l'API Bukkit moderne
            Class<?> craftMetaSkullClass = skullMeta.getClass();
            Method setProfileMethod = craftMetaSkullClass.getDeclaredMethod("setProfile", GameProfile.class);
            setProfileMethod.setAccessible(true);
            setProfileMethod.invoke(skullMeta, profile);

            this.is.setItemMeta(skullMeta);
            return this;

        } catch (Exception e) {
            // Fallback vers l'ancienne méthode si la nouvelle ne fonctionne pas
            try {
                GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                PropertyMap propertyMap = profile.getProperties();
                propertyMap.put("textures", new Property("textures", hash));

                // Créer un ResolvableProfile pour les versions récentes
                Class<?> resolvableProfileClass = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
                Object resolvableProfile = resolvableProfileClass.getConstructor(GameProfile.class).newInstance(profile);

                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(skullMeta, resolvableProfile);
                profileField.setAccessible(false);

                this.is.setItemMeta(skullMeta);
                return this;

            } catch (Exception ex) {
                // Dernière tentative avec l'API Bukkit standard
                try {
                    // Utiliser l'API Bukkit PlayerProfile (disponible depuis 1.18+)
                    org.bukkit.profile.PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
                    org.bukkit.profile.PlayerTextures textures = playerProfile.getTextures();

                    // Décoder le hash base64 pour obtenir l'URL
                    String decodedHash = new String(Base64.getDecoder().decode(hash));
                    com.google.gson.JsonObject jsonObject = new com.google.gson.JsonParser().parse(decodedHash).getAsJsonObject();
                    String url = jsonObject.getAsJsonObject("textures")
                            .getAsJsonObject("SKIN")
                            .get("url").getAsString();

                    textures.setSkin(new URL(url));
                    playerProfile.setTextures(textures);
                    skullMeta.setOwnerProfile(playerProfile);

                    this.is.setItemMeta(skullMeta);
                    return this;

                } catch (Exception finalEx) {
                    System.err.println("Erreur lors de l'application de la texture : " + finalEx.getMessage());
                    finalEx.printStackTrace();
                }
            }
        }

        return this;
    }

    /**
     * Set the displayname of the item.
     *
     * @param name The name to change it to.
     */
    public ItemBuilder setName(String name) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatUtil.translate(name));
        is.setItemMeta(im);
        return this;
    }

    /**
     * Hide all item flags (attributes, enchants, unbreakable, etc).
     */
    public ItemBuilder hideItemFlags() {
        ItemMeta im = is.getItemMeta();
        for (ItemFlag value : ItemFlag.values()) {
            im.addItemFlags(value);
        }
        is.setItemMeta(im);
        return this;
    }

    /**
     * Set the amount of items of the ItemStack.
     *
     * @param amount The amount to set it to.
     */
    public ItemBuilder setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    /**
     * Add an unsafe enchantment.
     *
     * @param ench  The enchantment to add.
     * @param level The level to put the enchant on.
     */
    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        is.addUnsafeEnchantment(ench, level);
        return this;
    }

    /**
     * Remove a certain enchant from the item.
     *
     * @param ench The enchantment to remove
     */
    public ItemBuilder removeEnchantment(Enchantment ench) {
        is.removeEnchantment(ench);
        return this;
    }

    /**
     * Set the skull owner for the item. Works on skulls only.
     *
     * @param owner The name of the skull's owner.
     */
    public ItemBuilder setSkullOwner(String owner) {
        try {
            SkullMeta im = (SkullMeta) is.getItemMeta();
            im.setOwner(owner);
            is.setItemMeta(im);
        } catch (ClassCastException expected) {
        }
        return this;
    }

    /**
     * Add an enchant to the item.
     *
     * @param ench  The enchant to add
     * @param level The level
     */
    public ItemBuilder addEnchant(Enchantment ench, int level) {
        ItemMeta im = is.getItemMeta();
        im.addEnchant(ench, level, true);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add multiple enchants at once.
     *
     * @param enchantments The enchants to add.
     */
    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        is.addEnchantments(enchantments);
        return this;
    }

    /**
     * Sets infinity durability on the item by setting the durability to Short.MAX_VALUE.
     */
    public ItemBuilder setInfinityDurability() {
        ItemMeta meta = is.getItemMeta();
        meta.setUnbreakable(true);
        is.setItemMeta(meta);
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(String... lore) {
        ItemMeta im = is.getItemMeta();
        List<String> list = new ArrayList<>();
        for (String s : lore) {
            list.add(ChatUtil.translate(s));
        }
        im.setLore(list);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        List<String> list = new ArrayList<>();
        for (String s : lore) {
            list.add(ChatUtil.translate(s));
        }
        im.setLore(list);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param line The lore to remove.
     */
    public ItemBuilder removeLoreLine(String line) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        if (!lore.contains(line)) return this;
        lore.remove(line);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param index The index of the lore line to remove.
     */
    public ItemBuilder removeLoreLine(int index) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        if (index < 0 || index > lore.size()) return this;
        lore.remove(index);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     */
    public ItemBuilder addLoreLine(String line) {
        line = ChatUtil.translate(line);
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) lore = new ArrayList<>(im.getLore());
        lore.add(line);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     * @param pos  The index of where to put it.
     */
    public ItemBuilder addLoreLine(String line, int pos) {
        line = ChatUtil.translate(line);
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        lore.set(pos, line);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Sets the dye color on an item.
     * <b>* Notice that this doesn't check for item type, sets the literal data of the dyecolor as durability.</b>
     *
     * @param color The color to put.
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder setDyeColor(DyeColor color) {
        this.is.setDurability(color.getDyeData());
        return this;
    }

    /**
     * Sets the dye color of a wool item. Works only on wool.
     *
     * @param color The DyeColor to set the wool item to.
     * @see ItemBuilder#setDyeColor(DyeColor)
     * @deprecated As of version 1.2 changed to setDyeColor.
     */
    @Deprecated
    public ItemBuilder setWoolColor(DyeColor color) {
        if (!is.getType().equals(Material.LEGACY_WOOL)) return this;
        this.is.setDurability(color.getWoolData());
        return this;
    }

    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     *
     * @param color The color to set it to.
     */
    public ItemBuilder setLeatherArmorColor(Color color) {
        try {
            LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
            im.setColor(color);
            is.setItemMeta(im);
        } catch (ClassCastException expected) {
        }
        return this;
    }

    /**
     * Retrieves the itemstack from the ItemBuilder.
     *
     * @return The itemstack created/modified by the ItemBuilder instance.
     */
    public ItemStack toItemStack() {
        return is;
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     * @return The ItemBuilder as JSON String
     */
    public String toJson() {
        return new Gson().toJson(is);
    }


    /**
     * Converts the JsonItemBuilder back to a ItemBuilder
     * @param json Which JsonItemBuilder should be converted
     */
    public static ItemStack fromJson(String json) {
        return new Gson().fromJson(json, ItemStack.class);
    }


}