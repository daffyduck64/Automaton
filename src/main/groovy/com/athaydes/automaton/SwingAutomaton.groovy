package com.athaydes.automaton

import javax.swing.*
import java.awt.*
import java.util.List

/**
 *
 * User: Renato
 */
class SwingAutomaton extends Automaton<SwingAutomaton> {

	private static instance

	/**
	 * Get the singleton instance of SwingAutomaton, which is lazily created.
	 * @return SwingAutomaton singleton instance
	 */
	static synchronized SwingAutomaton getUser( ) {
		if ( !instance ) instance = new SwingAutomaton()
		instance
	}

	protected SwingAutomaton( ) {}

	SwingAutomaton clickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).click()
	}

	SwingAutomaton clickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> clickOn( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingAutomaton doubleClickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).doubleClick()
	}

	SwingAutomaton doubleClickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> doubleClickOn( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		moveTo( { centerOf( component ) }, speed )
	}

	SwingAutomaton moveTo( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> moveTo( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingDragOn<SwingAutomaton> drag( Component component ) {
		def center = centerOf( component )
		new SwingDragOn( this, center.x, center.y )
	}

	static Point centerOf( Component component ) {
		assert component != null, 'Component could not be found'
		try {
			def center = component.locationOnScreen
			center.x += component.width / 2
			center.y += component.height / 2
			return center
		} catch ( IllegalComponentStateException ignore ) {
			throw new RuntimeException( "Component not showing on screen: " + component )
		}
	}

}

class Swinger extends Automaton<Swinger> {

	static final Map<String, Closure<Component>> DEFAULT_PREFIX_MAP =
		[
				'name:': SwingUtil.&lookup,
				'text:': SwingUtil.&text,
				'type:': SwingUtil.&type,
		].asImmutable()

	Component component
	protected delegate = SwingAutomaton.user
	Map<String, Closure<Component>> specialPrefixes

	/**
	 * Gets a new instance of <code>Swinger</code> using the given
	 * top-level component.
	 * <br/>
	 * The search space is limited to the given Component.
	 * @param component top level Swing component to use
	 * @return a new Swinger instance
	 */
	static Swinger getUserWith( Component component ) {
		new Swinger( specialPrefixes: DEFAULT_PREFIX_MAP, component: component )
	}

	/**
	 * @return Swinger whose root element is the first Window that can be found
	 * by calling {@code java.awt.Window.getWindows ( )} which is an instance of
	 * {@code JFrame}.
	 */
	static Swinger forSwingWindow( ) {
		def isJFrame = { it instanceof JFrame }
		if ( Window.windows && Window.windows.any( isJFrame ) ) {
			getUserWith( Window.windows.find( isJFrame ) )
		} else {
			throw new RuntimeException( 'Impossible to get any Swing window which is a JFrame' )
		}
	}

	protected Swinger( ) {}

	Component getAt( String selector ) {
		findPrefixed( ensurePrefixed( selector ) ) as Component
	}

	def <K> K getAt( Class<K> type ) {
		findPrefixed( 'type:', type.name ) as K
	}

	Swinger clickOn( Component component, Speed speed = DEFAULT ) {
		delegate.clickOn( component, speed )
		this
	}

	Swinger clickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		delegate.clickOn( components, pauseBetween, speed )
		this
	}

	Swinger clickOn( String selector, Speed speed = DEFAULT ) {
		def prefix_selector = ensurePrefixed selector
		delegate.clickOn( findPrefixed( prefix_selector ), speed )
		this
	}

	Swinger doubleClickOn( Component component, Speed speed = DEFAULT ) {
		delegate.doubleClickOn( component, speed )
		this
	}

	Swinger doubleClickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		delegate.doubleClickOn( components, pauseBetween, speed )
		this
	}

	Swinger doubleClickOn( String selector, Speed speed = DEFAULT ) {
		def prefix_selector = ensurePrefixed selector
		delegate.doubleClickOn( findPrefixed( prefix_selector ), speed )
		this
	}

	Swinger moveTo( Component component, Speed speed = DEFAULT ) {
		delegate.moveTo( component, speed )
		this
	}

	Swinger moveTo( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		delegate.moveTo( components, pauseBetween, speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		def prefix_selector = ensurePrefixed selector
		delegate.moveTo( findPrefixed( prefix_selector ), speed )
		this
	}

	SwingerDragOn drag( Component component ) {
		def center = SwingAutomaton.centerOf( component )
		new SwingerDragOn( this, center.x, center.y )
	}

	SwingerDragOn drag( String selector ) {
		def prefix_selector = ensurePrefixed selector
		drag( findPrefixed( prefix_selector ) )
	}

	protected List ensurePrefixed( String selector ) {
		def prefixes = specialPrefixes.keySet()
		def prefix = prefixes.find { selector.startsWith it }
		[ prefix ?: prefixes[ 0 ], prefix ? selector - prefix : selector ]
	}

	protected findPrefixed( String prefix, String selector ) {
		def target = specialPrefixes[ prefix ]( selector, component )
		if ( target ) target else
			throw new RuntimeException( "Unable to locate prefix=$prefix, selector=$selector" )
	}

}

class SwingDragOn<T extends Automaton<? extends Automaton>> extends DragOn<T> {

	protected SwingDragOn( T automaton, fromX, fromY ) {
		super( automaton, fromX, fromY )
	}

	T onto( Component component, Speed speed = Automaton.DEFAULT ) {
		def center = SwingAutomaton.centerOf( component )
		onto( center.x, center.y, speed )
	}

}

class SwingerDragOn extends SwingDragOn<Swinger> {

	protected SwingerDragOn( Swinger swinger, fromX, fromY ) {
		super( swinger, fromX, fromY )
	}

	Swinger onto( String selector, Speed speed = Automaton.DEFAULT ) {
		def prefix_selector = automaton.ensurePrefixed selector
		onto( automaton.findPrefixed( prefix_selector ), speed )
	}
}


