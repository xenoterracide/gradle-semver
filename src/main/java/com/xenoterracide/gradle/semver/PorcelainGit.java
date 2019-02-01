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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

final class PorcelainGit {
    private static final String VERSION_PREFIX = "v";
    private static final String TAG_PREFIX = "refs/tags/";
    private static final String VERSION_GLOB = VERSION_PREFIX + "[0-9]*.[0-9]*.[0-9]*";

    private final Git git;

    PorcelainGit( Git git ) {
        this.git = Objects.requireNonNull( git );
    }

    private static <T> T getLast( List<T> list ) {
        return list != null && !list.isEmpty() ? list.get( list.size() - 1 ) : null;
    }

    Ref findMostRecentTag() throws IOException {
        return getLast( getDb().getRefsByPrefix( TAG_PREFIX + VERSION_PREFIX ) );
    }

    private Repository getRepo() {
        return git.getRepository();
    }

    long countCommits( AnyObjectId a, AnyObjectId b )
            throws IncorrectObjectTypeException, MissingObjectException, GitAPIException {
        return StreamSupport.stream( git.log().addRange( a, b ).call().spliterator(), true ).count();
    }

    String describe() throws InvalidPatternException, GitAPIException,
            IOException {
        String version = git.describe().setMatch( VERSION_GLOB ).call();
        if ( version == null ) {
            Ref mostRecentTag = findMostRecentTag();
            if ( mostRecentTag != null ) {
                String vtag = StringUtils.removeStart( mostRecentTag.getName(), TAG_PREFIX );
                ObjectId head = getRepo().resolve( "HEAD" );
                long commitCount = countCommits( mostRecentTag.getPeeledObjectId(), head.toObjectId() );
                version = String.join( "-", vtag, String.valueOf( commitCount ),
                        'g' + head.name().substring( 0, 7 ), "SNAPSHOT"
                );

            }
        }
        return version;
    }

    private RefDatabase getDb() {
        return getRepo().getRefDatabase();
    }
}
