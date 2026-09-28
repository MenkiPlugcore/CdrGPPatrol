package id.menkiplugcore.cdrgppatrol.model;

import org.bukkit.Material;

public enum TrustLevel {
    MANAGER("Manager", "&d", Material.NETHER_STAR),
    BUILD("Build", "&a", Material.DIAMOND_PICKAXE),
    CONTAINER("Container", "&6", Material.CHEST),
    ACCESS("Access", "&b", Material.OAK_DOOR);

    private final String displayName;
    private final String color;
    private final Material material;

    TrustLevel(String displayName, String color, Material material) {
        this.displayName = displayName;
        this.color = color;
        this.material = material;
    }

    public String displayName() {
        return displayName;
    }

    public String color() {
        return color;
    }

    public Material material() {
        return material;
    }
}
