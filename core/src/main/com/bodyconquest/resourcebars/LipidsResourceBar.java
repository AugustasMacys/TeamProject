package main.com.bodyconquest.resourcebars;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import main.com.bodyconquest.constants.Resource;
import main.com.bodyconquest.handlers.AnimationWrapper;
import main.com.bodyconquest.rendering.BodyConquest;

/**
 * The type Lipids resource bar.
 */
public class LipidsResourceBar extends ResourceBar {

    /**
     * Instantiates a new Lipids resource bar.
     */
    public LipidsResourceBar() {
        setResourceType(Resource.LIPID);
        setOutline(new TextureRegion(new Texture("core/assets/lipid_outline.png")));
        setInside(new TextureRegion(new Texture("core/assets/lipids_inside.png")));
        setInsideTexturePath("core/assets/lipids_inside.png");
        setX(getX() + 2 * BodyConquest.V_WIDTH / 20.0f);
        walkAnimation = AnimationWrapper.getSpriteSheet(4, 1, 0.2f, getInsideTexturePath());
    }
}
