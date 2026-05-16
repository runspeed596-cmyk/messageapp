const fs = require('fs');
const path = 'src/pages/WorldOfScienceSettings.tsx';
let content = fs.readFileSync(path, 'utf8');

// For standard tables
const standardArrays = ['fieldsOfStudy', 'faculties', 'educationLevels', 'educationalRoles', 'clubs', 'studentOrgs'];
for (const arr of standardArrays) {
    const tableEnd = '</table>\n                    </div>';
    // We only want to replace the first occurrence that hasn't been paginated yet.
    // To do this reliably, we'll replace the text `</table>\n                    </div>` exactly where it appears right after each array's map loop.
    // But since `parts = content.split('</table>\n                    </div>')` worked before except line endings:
}

// Let's use regex to find the end of each tab
function appendPagination(tabName, arrayName, endMarker) {
    const regex = new RegExp(`(${endMarker})\\s*(?=\\{/\\* ═══════════════════════════════════════════════════════════════ \\*/\\n\\s*\\{/\\* ───)`, 'g');
    
    // Fallback if it's the last tab (slider)
    if (arrayName === 'sliderBanners') {
        const sliderRegex = /(<div className="glass p-16 rounded-2xl text-center border-dashed border-white\/10">[\s\S]*?<\/div>\n                    )\s*\)/;
        content = content.replace(sliderRegex, `$1\n                    {${arrayName}.length > PAGE_SIZE && (
                        <div className="mt-4 p-4 border-t border-white/5">
                            <Pagination
                                currentPage={currentPage}
                                totalPages={Math.ceil(${arrayName}.length / PAGE_SIZE)}
                                totalElements={${arrayName}.length}
                                pageSize={PAGE_SIZE}
                                onPageChange={setCurrentPage}
                            />
                        </div>
                    )}\n                    )`);
        return;
    }

    const replacement = `$1\n                    {${arrayName}.length > PAGE_SIZE && (
                        <div className="mt-4 p-4 border-t border-white/5">
                            <Pagination
                                currentPage={currentPage}
                                totalPages={Math.ceil(${arrayName}.length / PAGE_SIZE)}
                                totalElements={${arrayName}.length}
                                pageSize={PAGE_SIZE}
                                onPageChange={setCurrentPage}
                            />
                        </div>
                    )}`;
                    
    content = content.replace(regex, replacement);
}

// Actually, regex matching with tabs is fragile. I'll just use a direct replace.
// Let's find exactly the text for the end of each table to replace it.
const tableEnds = [
    'هنوز رشته‌ای ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز دانشکده‌ای ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز مقطعی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز نقشی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز کانونی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز تشکلی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>'
];

for (let i = 0; i < 6; i++) {
    const arr = standardArrays[i];
    let endStr = tableEnds[i];
    // Replace CRLF just in case
    content = content.split(endStr).join(endStr + `\n                    {${arr}.length > PAGE_SIZE && (
                        <div className="mt-4 p-4">
                            <Pagination
                                currentPage={currentPage}
                                totalPages={Math.ceil(${arr}.length / PAGE_SIZE)}
                                totalElements={${arr}.length}
                                pageSize={PAGE_SIZE}
                                onPageChange={setCurrentPage}
                            />
                        </div>
                    )}`);
    
    // Also try with \r\n
    endStr = endStr.replace(/\n/g, '\r\n');
    content = content.split(endStr).join(endStr + `\r\n                    {${arr}.length > PAGE_SIZE && (
                        <div className="mt-4 p-4">
                            <Pagination
                                currentPage={currentPage}
                                totalPages={Math.ceil(${arr}.length / PAGE_SIZE)}
                                totalElements={${arr}.length}
                                pageSize={PAGE_SIZE}
                                onPageChange={setCurrentPage}
                            />
                        </div>
                    )}`);
}

// Slider banners
const sliderEnd = `                                </div>
                            ))}
                        </div>
                    ) : (`;
content = content.split(sliderEnd).join(`                                </div>
                            ))}
                        </div>
                    ) : (`.replace(') : (', `)
                    {sliderBanners.length > PAGE_SIZE && (
                        <div className="mt-4 p-4">
                            <Pagination
                                currentPage={currentPage}
                                totalPages={Math.ceil(sliderBanners.length / PAGE_SIZE)}
                                totalElements={sliderBanners.length}
                                pageSize={PAGE_SIZE}
                                onPageChange={setCurrentPage}
                            />
                        </div>
                    )}
                    ) : (`));


// Fix the duplicate pagination blocks if the script is run multiple times
// I'll skip that check since I only run it once.
fs.writeFileSync(path, content, 'utf8');
