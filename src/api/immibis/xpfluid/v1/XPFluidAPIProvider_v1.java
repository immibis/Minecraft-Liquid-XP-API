package api.immibis.xpfluid.v1;

import net.minecraftforge.fluids.FluidStack;

/**
 * DO NOT MODIFY THIS FILE. EVER.
 */
public interface XPFluidAPIProvider_v1 {
	/**
	 * Called to convert fluid into XP.
	 * If this fluid does not represent an integer amount of XP, it should be rounded down.
	 * 
	 * May not return null.
	 * 
	 * The parameter will ALWAYS be a recognized fluid (that isXPFluid returned true for, and that
	 * this provider is registered for).
	 * 
	 * Returns the amount of XP contained in this fluid, and the amount of fluid that XP corresponds to
	 * (which might be less, in case of rounding), even if those amounts are zero.
	 */
	public XPFluidAPI_v1.FluidToXPResult convertFluidToXP(FluidStack fluid);
	
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
	public XPFluidAPI_v1.XPToFluidResult convertXPToFluid(int xp);

	/**
	 * Returns true if the given fluid stack is recognized as an XP fluid, ignoring the amount.
	 * (Note: This may be called with a zero amount, or an amount less than one unit of XP)
	 * 
	 * The parameter will always be a fluid that this provider was registered for.
	 */
	public boolean isXPFluid(FluidStack fluid);
	
}
