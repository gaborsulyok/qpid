/*
 *
 * Copyright (c) 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include "qpid/sys/FileSysDir.h"
#include "qpid/sys/StrError.h"
#include "qpid/Exception.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <direct.h>
#include <errno.h>
#include <windows.h>
#include <strsafe.h>


namespace qpid {
namespace sys {

bool FileSysDir::exists (void) const
{
    const  char *cpath = dirPath.c_str ();
    struct _stat  s;
    if (::_stat(cpath, &s)) {
        if (errno == ENOENT) {
            return false;
        }
        throw qpid::Exception (strError(errno) +
                               ": Can't check directory: " + dirPath);
    }
    if (s.st_mode & _S_IFDIR)
        return true;
    throw qpid::Exception(dirPath + " is not a directory");
}

void FileSysDir::mkdir(void)
{
    if (::_mkdir(dirPath.c_str()) == -1)
        throw Exception ("Can't create directory: " + dirPath);
}

void FileSysDir::forEachFile(Callback cb) const {

    WIN32_FIND_DATAA findFileData;
    char             szDir[MAX_PATH];
    size_t           dirPathLength;
    HANDLE           hFind = INVALID_HANDLE_VALUE;

    // create dirPath+"\*" in szDir
    StringCchLength (dirPath.c_str(), MAX_PATH, &dirPathLength);

    if (dirPathLength > (MAX_PATH - 3)) {
        throw Exception ("Directory path is too long: " + dirPath);
    }

    StringCchCopy(szDir, MAX_PATH, dirPath.c_str());
    StringCchCat(szDir, MAX_PATH, TEXT("\\*"));

    // Special work for first file
    hFind = FindFirstFileA(szDir, &findFileData);
    if (INVALID_HANDLE_VALUE == hFind) {
        return;
    }

    // process everything that isn't a directory
    do {
        if (!(findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
            std::string fileName(dirPath);
            fileName += "\\";
            fileName += findFileData.cFileName;
            cb(fileName);
        }
    } while (FindNextFile(hFind, &findFileData) != 0);
}

}} // namespace qpid::sys
