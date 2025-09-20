package com.cbo.core

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Core module that brings together all core functionality.
 * This module acts as a coordinator and exposes all core modules through API dependencies.
 * 
 * Included modules:
 * - core-domain: Domain entities, repositories interfaces, and use cases
 * - core-data: Repository implementations and data sources
 * - core-database: Room database, DAOs, and entities
 * - core-common: Shared utilities, base classes, and validation
 * - core-navigation: Navigation destinations and related logic
 * - core-session: Session management and user authentication state
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    // This module primarily acts as a coordinator
    // All DI modules are in their respective core-* modules
}
