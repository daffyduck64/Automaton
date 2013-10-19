package com.athaydes.automaton

import java.awt.*

/**
 *
 * User: Renato
 */
class SwingAutomaton extends Automaton<SwingAutomaton> {

	private static instance

	static synchronized SwingAutomaton getUser( ) {
		if ( !instance ) instance = new SwingAutomaton()
		instance
	}

	protected SwingAutomaton( ) {}

	SwingAutomaton clickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).click()
	}

	SwingAutomaton doubleClickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).doubleClick()
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf component
		move( currPos, target, speed )
	}

	SwingDragOn<SwingAutomaton> drag( Component component ) {
		def center = centerOf( component )
		new SwingDragOn( this, center.x, center.y )
	}

	static Point centerOf( Component component ) {
		def center = component.locationOnScreen
		center.x += component.width / 2
		center.y += component.height / 2
		return center
	}

}

class Swinger extends Automaton<Swinger> {

	protected Component component
	private delegate = SwingAutomaton.user

	static Swinger getUserWith( Component component ) {
		new Swinger( component: component )
	}

	Swinger clickOn( String selector, Speed speed = DEFAULT ) {
		delegate.clickOn( SwingUtil.lookup( selector, component ), speed )
		this
	}

	Swinger doubleClickOn( String selector, Speed speed = DEFAULT ) {
		delegate.doubleClickOn( SwingUtil.lookup( selector, component ), speed )
		this
	}

	Swinger clickOn( Component component, Speed speed = DEFAULT ) {
		delegate.clickOn( component, speed )
		this
	}

	Swinger doubleClickOn( Component component, Speed speed = DEFAULT ) {
		delegate.doubleClickOn( component, speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		def component = SwingUtil.lookup( selector, component )
		delegate.moveTo( component, speed )
		this
	}

	Swinger moveTo( Component component, Speed speed = DEFAULT ) {
		delegate.moveTo( component, speed )
		this
	}

	SwingerDragOn drag( Component component ) {
		def center = SwingAutomaton.centerOf( component )
		new SwingerDragOn( this, center.x, center.y )
	}

	SwingerDragOn drag( String selector ) {
		def component = SwingUtil.lookup( selector, component )
		drag( component )
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
		def component = SwingUtil.lookup( selector, automaton.component )
		onto( component, speed )
	}
}
