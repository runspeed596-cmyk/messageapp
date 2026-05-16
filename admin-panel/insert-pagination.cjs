const fs = require('fs');
const path = 'src/pages/WorldOfScienceSettings.tsx';
let content = fs.readFileSync(path, 'utf8');

// Normalize line endings
content = content.replace(/\r\n/g, '\n');

const arrays = ['fieldsOfStudy', 'faculties', 'educationLevels', 'educationalRoles', 'clubs', 'studentOrgs', 'sliderBanners'];
let parts = content.split('</table>\n                    </div>');

if (parts.length === arrays.length + 1) { // 7 tables + 1 empty string
    for (let i = 0; i < arrays.length; i++) {
        const arr = arrays[i];
        const paginationBlock = `\n                    {${arr}.length > PAGE_SIZE && (
                        <div className="mt-4 p-4 border-t border-white/5">
                            <Pagination
                                currentPage={currentPage}
                                totalPages={Math.ceil(${arr}.length / PAGE_SIZE)}
                                totalElements={${arr}.length}
                                pageSize={PAGE_SIZE}
                                onPageChange={setCurrentPage}
                            />
                        </div>
                    )}`;
        parts[i] += `</table>\n                    </div>${paginationBlock}`;
    }
    content = parts.join('');
    fs.writeFileSync(path, content, 'utf8');
    console.log('Pagination inserted successfully!');
} else {
    console.error(`Expected ${arrays.length + 1} parts but found ${parts.length} parts. Please check.`);
}
