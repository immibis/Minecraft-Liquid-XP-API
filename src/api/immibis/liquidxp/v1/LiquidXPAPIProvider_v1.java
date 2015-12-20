package api.immibis.liquidxp.v1;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * DO NOT MODIFY THIS FILE. EVER.
 */
public interface LiquidXPAPIProvider_v1 {
	/**
	 * Returns a short, human-readable name of this provider.
	 * 
	 * This may be used for configuration purposes - the user might need to select a provider
	 * by specifying its name.
	 * 
	 * For ease of configuration, the name should not contain spaces.
	 */
	public String getName();

	/**
	 * Called to convert fluid into XP.
	 * If this fluid does not represent an integer amount of XP, it should be rounded down.
	 * 
	 * Returns null if this is an unrecognized fluid. Otherwise, returns the amount of
	 * XP contained in this fluid, and the amount of fluid that XP corresponds to
	 * (which might be less, in case of rounding), even if those amounts are zero.
	 */
	public LiquidXPAPI_v1.FluidToXPResult convertFluidToXP(FluidStack fluid);
	
	/**
	 * Called to convert XP into fluid.
	 * If the amount of XP can't be exactly represented by fluid, it should be rounded down.
	 * 
	 * May not return null.
	 * 
	 * Returns a fluid representing (at most) the given amount of XP, plus the amount
	 * of XP that fluid corresponds to (which might be less, in case of rounding), even
	 * if those amounts are zero.
	 * 
	 * For efficiency purposes, the *fluid* returned may be null (meaning an empty fluid stack),
	 * even though the result itself may not be.
	 */
	public LiquidXPAPI_v1.XPToFluidResult convertXPToFluid(int xp);

	/**
	 * Returns true if the given fluid stack is recognized as an XP fluid.
	 */
	public boolean isLiquidXP(Fluid fluid);
	
}
