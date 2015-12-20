package api.immibis.liquidxp.v1;

import java.util.Arrays;

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
	
	private static LiquidXPAPIProvider_v1[] providers = new LiquidXPAPIProvider_v1[0];
	
	/**
	 * Call this during init to register your provider.
	 * Calling it in preinit is discouraged unless you need to override another mod's provider
	 * (registered in init) for some reason.
	 */
	public static void addProvider(LiquidXPAPIProvider_v1 provider) {
		if(provider == null)
			throw new IllegalArgumentException("argument is null");
		if(provider.getName() == null)
			throw new IllegalArgumentException("argument is null");
		if(Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
			throw new IllegalStateException("Providers must be registered in init stage or earlier");
			
		providers = Arrays.copyOf(providers, providers.length + 1);
		providers[providers.length - 1] = provider;
	}
	
	/**
	 * Call this after init if you need a list of providers.
	 * For example, you will need to provide a way for the user to choose which kind of fluid their
	 * XP should be converted into. You can do this by selecting a provider from the returned list.
	 */
	public static LiquidXPAPIProvider_v1[] getProviders() {
		if(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
			throw new IllegalStateException("Provider list may be retrieved during/after postinit stage");
		return providers.clone();
	}
	
	public static final class XPToFluidResult {
		public final int xpUsed;
		public final FluidStack fluidReturned;
		
		public XPToFluidResult(int xp, FluidStack fluid) {
			this.xpUsed = xp;
			this.fluidReturned = fluid;
			if(xp < 0 || fluid == null || fluid.amount < 0)
				throw new IllegalArgumentException();
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
		}
		
		public static final FluidToXPResult ZERO = new FluidToXPResult(0, 0);
	}
	
	/**
	 * In case the given amount of fluid doesn't represent an integer amount of XP, this will
	 * round down.
	 * In case the given amount of fluid doesn't represent an integerXP can't be represented exactly,
	 * this adds a small amount of random noise.
	 */
	public static FluidToXPResult convertFluidToXP(FluidStack fluid) {
		for(LiquidXPAPIProvider_v1 provider : providers) {
			FluidToXPResult ret = provider.convertFluidToXP(fluid);
			if(ret != null)
				return ret;
		}
		return null;
	}
	
	/**
	 * Returns true if the given fluid stack is a valid XP fluid.
	 */
	public static boolean isLiquidXP(Fluid fluid) {
		for(LiquidXPAPIProvider_v1 provider : providers)
			if(provider.isLiquidXP(fluid))
				return true;
		return false;
	}
}
