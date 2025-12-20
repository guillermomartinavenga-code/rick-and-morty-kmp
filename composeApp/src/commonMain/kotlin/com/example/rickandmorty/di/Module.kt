package com.example.rickandmorty.di

import com.example.rickandmorty.data.remote.CharacterService
import com.example.rickandmorty.data.remote.createCharacterService
import com.example.rickandmorty.data.repository.CharacterRepositoryImpl
import com.example.rickandmorty.domain.repository.CharacterRepository
import com.example.rickandmorty.domain.use_case.GetCharactersUseCase

/**
 * A simple dependency injection module to provide instances of the required classes.
 */
object Module {
    /**
     * Provides an instance of [CharacterService].
     */
    private val characterService: CharacterService by lazy {
        createCharacterService()
    }

    /**
     * Provides an instance of [CharacterRepository].
     */
    private val characterRepository: CharacterRepository by lazy {
        CharacterRepositoryImpl(characterService)
    }

    /**
     * Provides an instance of [GetCharactersUseCase].
     */
    val getCharactersUseCase: GetCharactersUseCase by lazy {
        GetCharactersUseCase(characterRepository)
    }
}
