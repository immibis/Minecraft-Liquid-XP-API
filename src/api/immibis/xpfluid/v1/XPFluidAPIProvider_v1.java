package api.immibis.xpfluid.v1;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * DO NOT MODIFY THIS FILE. EVER.
 */
public class XPFluidAPIProvider_v1 {
	
	private final double mbPerXP;
	private final Fluid fluid;
	
	/**
	 * fluid is your fluid. You should register a provider with your own mod's fluid,
	 * even if your mod's fluid failed to register in the FluidRegistry.
	 * 
	 * mbPerXP is the amount of millibuckets of fluid that one experience point is worth.
	 */
	public XPFluidAPIProvider_v1(Fluid fluid, double mbPerXP) {
		this.fluid = fluid;
		this.mbPerXP = mbPerXP;
		if(fluid == null || mbPerXP <= 0.00000001 || mbPerXP > 1000000.0)
			throw new IllegalArgumentException();
	}
	
	public final double convertXPToMB(double xp) {
		return xp * mbPerXP;
	}
	
	public final double convertMBToXP(double mb) {
		return mb / mbPerXP;
	}
	
	public final Fluid getFluid() {
		return fluid;
	}
	
	public final boolean isXPFluid(FluidStack stack) {
		return stack.getFluid() == fluid && isXPFluidInternal(stack);
	}
	
	@SuppressWarnings("deprecation")
	public final FluidStack createFluidStack(int amount) {
		if(amount < 0)
			throw new IllegalArgumentException("amount < 0: " + amount);
		if(amount == 0)
			return null;
		
		FluidStack result = createFluidStackInternal(amount);
		if(result == null)
			throw new AssertionError("The creator of "+fluid.getName()+" fucked up their createFluidStackInternal implementation - it returned null.");
		if(result.getFluid() != fluid) {
			if(result.fluid == fluid)
				// This Fluid was overridden in the FluidRegistry by another mod's Fluid with the same name
				throw new IllegalArgumentException("You're calling into a fluid API provider for a non-default fluid.");
			
			throw new AssertionError("The creator of "+fluid.getName()+" fucked up their createFluidStackInternal implementation.");
		}
		if(result.amount != amount)
			throw new AssertionError("The creator of "+fluid.getName()+" fucked up their createFluidStackInternal implementation. (Amount of fluid expected: "+amount+"; amount actually returned: "+result.amount+")");
		return result;
	}
	
	/**
	 * Override this if your fluid uses NBT tags for identification.
	 */
	protected boolean isXPFluidInternal(FluidStack stack) {
		return true;
	}
	
	/**
	 * Override this if your fluid uses NBT tags for identification.
	 */
	protected FluidStack createFluidStackInternal(int amount) {
		return new FluidStack(fluid, amount);
	}
}
