const fs = require('fs');
const path = 'src/pages/WorldOfScienceSettings.tsx';
let content = fs.readFileSync(path, 'utf8');

// Add Pagination import
if (!content.includes('Pagination')) {
    content = content.replace("import { Users, Image as ImageIcon, Upload } from 'lucide-react';", "import { Users, Image as ImageIcon, Upload } from 'lucide-react';\nimport Pagination from '../components/Pagination';");
}

// Add state
if (!content.includes('const [currentPage, setCurrentPage] = useState<number>(0);')) {
    content = content.replace("const [error, setError] = useState<string>('');", "const [error, setError] = useState<string>('');\n    const [currentPage, setCurrentPage] = useState<number>(0);\n    const PAGE_SIZE = 20;\n\n    useEffect(() => {\n        setCurrentPage(0);\n    }, [activeTab]);");
}

content = content.replace('.map((field, index) => (', '.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                    .map((field, index) => (');
content = content.replace('.map((faculty, index) => (', '.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                    .map((faculty, index) => (');
content = content.replace('.map((level, index) => (', '.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                    .map((level, index) => (');
content = content.replace('.map((role, index) => (', '.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                    .map((role, index) => (');
content = content.replace('clubs.sort((a, b) => a.displayOrder - b.displayOrder).map((club, index) => (', 'clubs.sort((a, b) => a.displayOrder - b.displayOrder)\n                                .slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                .map((club, index) => (');
content = content.replace('studentOrgs.sort((a, b) => a.displayOrder - b.displayOrder).map((org, index) => (', 'studentOrgs.sort((a, b) => a.displayOrder - b.displayOrder)\n                                .slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                .map((org, index) => (');
content = content.replace('{sliderBanners.map((banner) => (', '{sliderBanners.sort((a, b) => a.displayOrder - b.displayOrder)\n                                .slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE)\n                                .map((banner) => (');

const tableEnds = [
    'هنوز رشته‌ای ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز دانشکده‌ای ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز مقطعی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز نقشی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز کانونی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز تشکلی ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>',
    'هنوز اسلایدری ثبت نشده است\n                                        </td>\n                                    </tr>\n                                )}\n                            </tbody>\n                        </table>\n                    </div>'
];

const arrays = ['fieldsOfStudy', 'faculties', 'educationLevels', 'educationalRoles', 'clubs', 'studentOrgs', 'sliderBanners'];

for (let i = 0; i < tableEnds.length; i++) {
    const arr = arrays[i];
    const replacement = tableEnds[i] + '\n                    {'+arr+'.length > PAGE_SIZE && (\n                        <div className="mt-4 p-4">\n                            <Pagination\n                                currentPage={currentPage}\n                                totalPages={Math.ceil('+arr+'.length / PAGE_SIZE)}\n                                totalElements={'+arr+'.length}\n                                pageSize={PAGE_SIZE}\n                                onPageChange={setCurrentPage}\n                            />\n                        </div>\n                    )}';
    content = content.replace(tableEnds[i], replacement);
}

fs.writeFileSync(path, content, 'utf8');
console.log('Updated WorldOfScienceSettings.tsx successfully');
