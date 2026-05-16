const fs = require('fs');
const path = 'src/pages/WorldOfScienceSettings.tsx';
let content = fs.readFileSync(path, 'utf8');

const clubsTableEndRegex = /(\{clubs\.length === 0[\s\S]*?<\/table>\s*<\/div>)/;
content = content.replace(clubsTableEndRegex, '$1\n                    {clubs.length > PAGE_SIZE && (\n                        <div className=\"mt-4 p-4 border-t border-white/5\">\n                            <Pagination\n                                currentPage={currentPage}\n                                totalPages={Math.ceil(clubs.length / PAGE_SIZE)}\n                                totalElements={clubs.length}\n                                pageSize={PAGE_SIZE}\n                                onPageChange={setCurrentPage}\n                            />\n                        </div>\n                    )}');

const orgsTableEndRegex = /(\{studentOrgs\.length === 0[\s\S]*?<\/table>\s*<\/div>)/;
content = content.replace(orgsTableEndRegex, '$1\n                    {studentOrgs.length > PAGE_SIZE && (\n                        <div className=\"mt-4 p-4 border-t border-white/5\">\n                            <Pagination\n                                currentPage={currentPage}\n                                totalPages={Math.ceil(studentOrgs.length / PAGE_SIZE)}\n                                totalElements={studentOrgs.length}\n                                pageSize={PAGE_SIZE}\n                                onPageChange={setCurrentPage}\n                            />\n                        </div>\n                    )}');

fs.writeFileSync(path, content, 'utf8');
console.log('Done!');
