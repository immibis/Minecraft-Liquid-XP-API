package api.immibis.liquidxp.v1;

import java.util.IdentityHashMap;
import java.util.Map;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Main entry point to the Liquid XP API.
 * 
 * DO NOT MODIFY THIS FILE. EVER.
 */
public final class LiquidXPAPI_v1 {
	private LiquidXPAPI_v1() {throw new RuntimeException();}
	
	private static Map<Fluid, LiquidXPAPIProvider_v1> providers = new IdentityHashMap<>();
	
	/**
	 * Call this during init to register your provider.
	 * Calling it in preinit is discouraged unless you need to override another mod's provider
	 * (registered in init) for some reason.
	 */
	public static void addProvider(final Fluid fluid, final LiquidXPAPIProvider_v1 provider) {
		if(provider == null)
			throw new IllegalArgumentException("provider is null");
		if(fluid == null)
			throw new IllegalArgumentException("fluid is null");
		
		if(Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
			throw new IllegalStateException("Providers must be registered in init stage or earlier");
		
		if(providers.containsKey(fluid))
			throw new IllegalStateException("Fluid already registered: "+fluid+" ("+fluid.getName()+")");
		
		providers.put(fluid, new LiquidXPAPIProvider_v1() {
			@SuppressWarnings("deprecation")
			@Override
			public XPToFluidResult convertXPToFluid(int xp) {
				XPToFluidResult result = provider.convertXPToFluid(xp);
				
				if(result == null)
					throw new AssertionError("Provider "+provider.getClass().getName()+" returned null from convertXPToFluid for: "+fluid);
				
				if(result.fluidReturned != null) {
					if(result.fluidReturned.fluid != fluid)
						throw new AssertionError("Provider "+provider.getClass().getName()+" returned wrong fluid "+result.fluidReturned.fluid+" from convertXPToFluid (instead of "+fluid+")");
					if(result.fluidReturned.getFluid() != fluid)
						throw new AssertionError("Provider returned non-default fluid "+result.fluidReturned.fluid+" (overridden by "+result.fluidReturned.getFluid()+"). This most likely indicates that a non-default fluid was passed to LiquidXPAPI_v1.convertXPToFluid.");
				}
				
				return result;
			}
			
			@Override
			public FluidToXPResult convertFluidToXP(FluidStack fluid) {
				FluidToXPResult result = provider.convertFluidToXP(fluid);
				
				if(result == null)
					throw new AssertionError("Provider "+provider.getClass().getName()+" returned null from convertFluidToXP for: "+fluid);

				return result;
			}

			@Override
			public boolean isXPFluid(FluidStack testFluid) {
				return provider.isXPFluid(testFluid);
			}
		});
	}
	
	public static Fluid[] getXPFluids() {
		if(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
			throw new IllegalStateException("Fluid list may be retrieved after init stage");
		return providers.keySet().toArray(new Fluid[0]);
	}
	
	public static final class XPToFluidResult {
		public final int xpUsed;
		public final FluidStack fluidReturned;
		
		public XPToFluidResult(int xp, FluidStack fluid) {
			if(fluid != null) {
				if(fluid.amount == 0)
					fluid = null;
				else if(fluid.amount < 0)
					throw new IllegalArgumentException("negative fluid amount "+fluid.amount);
			}
			
			if(xp < 0)
				throw new IllegalArgumentException("negative XP amount "+xp);
			
			if((xp == 0) != (fluid == null))
				throw new IllegalArgumentException(); // zero XP is allowed if-and-only-if zero fluid
			
			this.xpUsed = xp;
			this.fluidReturned = fluid;
		}
		
		public static final XPToFluidResult ZERO = new XPToFluidResult(0, null);
	}
	
	public static final class FluidToXPResult {
		public final int fluidUsed;
		public final int xpReturned;
		
		public FluidToXPResult(int fluid, int xp) {
			this.fluidUsed = fluid;
			this.xpReturned = xp;
			if(xp < 0 || fluid < 0)
				throw new IllegalArgumentException();
			if((xp == 0) != (fluid == 0))
				throw new IllegalArgumentException(); // zero XP is allowed if-and-only-if zero fluid
		}
		
		public static final FluidToXPResult ZERO = new FluidToXPResult(0, 0);
	}
	
	/**
	 * Converts XP into a fluid. You MUST specify the target fluid - generally,
	 * this is either your own mod's fluid, or it's configurable.
	 * 
	 * The target fluid MUST be one contained in the array that getXPFluids() returns.
	 * It also MUST be a "default fluid" - i.e. one that FluidRegistry.getFluid might return.
	 * (If multiple mods register fluids with the same name, only one of them will be default.
	 * This is also saved per-world and per-server, so it can change when loading a world)
	 * 
	 * xpAmount must be positive.
	 */
	public static XPToFluidResult convertXPToFluid(Fluid targetFluid, int xpAmount) {
		if(xpAmount <= 0)
			throw new IllegalArgumentException("xp amount "+xpAmount);
		
		LiquidXPAPIProvider_v1 provider = providers.get(targetFluid);
		if(provider == null)
			throw new IllegalArgumentException("Fluid not registered: "+targetFluid);

		return provider.convertXPToFluid(xpAmount);
	}
	
	/**
	 * Converts a fluid back into XP.
	 * 
	 * In case the given amount of fluid doesn't represent an integer amount of XP, this will
	 * round down.
	 * 
	 * Returns the amount of fluid actually used, as well as the amount of XP, in case of rounding.
	 * 
	 * Returns null if this is not a recognized XP fluid.
	 */
	public static FluidToXPResult convertFluidToXP(FluidStack fluid) {
		LiquidXPAPIProvider_v1 provider = providers.get(fluid.getFluid());
		if(provider != null) {
			if(provider.isXPFluid(fluid)) {
				return provider.convertFluidToXP(fluid);
			}
		}
		return null;
	}
	
	/**
	 * Returns true if the given fluid stack is a recognized XP fluid.
	 */
	public static boolean isXPFluid(FluidStack fluid) {
		LiquidXPAPIProvider_v1 provider = providers.get(fluid.getFluid());
		if(provider != null) {
			return provider.isXPFluid(fluid);
		}
		return false;
	}
}
