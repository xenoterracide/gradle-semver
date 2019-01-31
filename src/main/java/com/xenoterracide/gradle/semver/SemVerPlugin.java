/*
 * Copyright 2018 Caleb Cushing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xenoterracide.gradle.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * inspired by @see <a href="https://github.com/cinnober/semver-git/">Cinnober's SemVer Git</a>
 */
public class SemVerPlugin implements Plugin<Project> {
    private final Logger log = LoggerFactory.getLogger( SemVerPlugin.class );


    @Override
    public void apply( Project project ) {
        try {
            Repository repo = new FileRepositoryBuilder()
                    .readEnvironment()
                    .findGitDir( project.getProjectDir() )
                    .build();

            Optional.ofNullable( new PorcelainGit( new Git( repo ) ).describe() )
                    .map( v -> v.substring( 1 ) )
                    .map( v -> v.contains( "g" ) ? v + "-SNAPSHOT" : v )
                    .ifPresent( project::setVersion );
        } catch ( IOException | InvalidPatternException | GitAPIException e ) {
            log.error( "", e );
        }
    }
}
