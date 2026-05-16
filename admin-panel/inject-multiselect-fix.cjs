const fs = require('fs');
const path = 'src/pages/OfficialChannelsGroups.tsx';
let content = fs.readFileSync(path, 'utf8');

// The multi-select UI
const functionDefinition = `
    const renderMultiSelect = (
        label: string,
        options: { value: string, label: string }[],
        value: string | undefined,
        onChange: (val: string | undefined) => void,
        disabled = false,
        disabledTooltip?: string
    ) => {
        const selectedList = value ? value.split(',').filter(Boolean) : [];
        
        return (
            <div className="space-y-1">
                <label className="text-xs font-bold text-slate-500">{label}</label>
                {selectedList.length > 0 && (
                    <div className="flex flex-wrap gap-1 mb-2">
                        {selectedList.map(item => {
                            const opt = options.find(o => o.value === item);
                            return (
                                <span key={item} className="bg-indigo-500/20 text-indigo-300 text-[10px] px-2 py-1 rounded-md flex items-center gap-1">
                                    {opt ? opt.label : item}
                                    <button type="button" onClick={() => {
                                        const newList = selectedList.filter(i => i !== item);
                                        onChange(newList.length > 0 ? newList.join(',') : undefined);
                                    }} className="hover:text-white"><X size={10} /></button>
                                </span>
                            );
                        })}
                    </div>
                )}
                <select
                    value=""
                    onChange={(e) => {
                        const val = e.target.value;
                        if (!val) return;
                        if (!selectedList.includes(val)) {
                            onChange([...selectedList, val].join(','));
                        }
                    }}
                    className="w-full glass bg-white/5 border-white/5 p-3 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 outline-none text-sm"
                    disabled={disabled}
                    title={disabled ? disabledTooltip : ''}
                >
                    <option value="" className="bg-slate-800">انتخاب کنید...</option>
                    {options.filter(o => !selectedList.includes(o.value)).map(o => (
                        <option key={o.value} value={o.value} className="bg-slate-800">{o.label}</option>
                    ))}
                </select>
            </div>
        );
    };

    return (
`;

if (!content.includes('const renderMultiSelect')) {
    content = content.replace('    return (\n        <div className="space-y-8 animate-in fade-in duration-500">', functionDefinition + '        <div className="space-y-8 animate-in fade-in duration-500">');
}

function generateTargetingFields(formName) {
    const isChannel = formName === 'channelForm';
    const formVar = isChannel ? 'channelForm' : 'groupForm';
    const setFormFn = isChannel ? 'setChannelForm' : 'setGroupForm';

    return `<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {renderMultiSelect(
                                            "استان",
                                            provinces.map(p => ({ value: p, label: p })),
                                            ${formVar}.targetProvince,
                                            (val) => {
                                                ${setFormFn}({ ...${formVar}, targetProvince: val, targetCity: undefined });
                                                if (val) {
                                                    const arr = val.split(',');
                                                    loadCities(arr[arr.length - 1]);
                                                } else {
                                                    loadCities("");
                                                }
                                            }
                                        )}
                                        {renderMultiSelect(
                                            "شهر",
                                            cities.map(c => ({ value: c, label: c })),
                                            ${formVar}.targetCity,
                                            (val) => ${setFormFn}({ ...${formVar}, targetCity: val }),
                                            !${formVar}.targetProvince,
                                            "ابتدا استان را انتخاب کنید"
                                        )}
                                        {renderMultiSelect(
                                            "دانشگاه",
                                            universities.map(u => ({ value: u.name, label: u.name })),
                                            ${formVar}.targetUniversity,
                                            (val) => ${setFormFn}({ ...${formVar}, targetUniversity: val })
                                        )}
                                        {renderMultiSelect(
                                            "وزارت مربوطه",
                                            [
                                                "وزارت علوم", "وزارت بهداشت", "پیام نور", "دانشگاه آزاد", "فنی حرفه ای",
                                                "منابع طبیعی", "علمی کاربردی", "غیرانتفاعی", "ملی مهارت", "علوم قرآن و معارف",
                                                "هنر", "موسسه آموزش عالی", "فرهنگیان", "علوم پزشکی"
                                            ].map(m => ({ value: m, label: m })),
                                            ${formVar}.targetMinistry,
                                            (val) => ${setFormFn}({ ...${formVar}, targetMinistry: val })
                                        )}
                                        {renderMultiSelect(
                                            "رشته تحصیلی",
                                            fieldsOfStudy.map(f => ({ value: f.name, label: f.name })),
                                            ${formVar}.targetFieldOfStudy,
                                            (val) => ${setFormFn}({ ...${formVar}, targetFieldOfStudy: val })
                                        )}
                                        {renderMultiSelect(
                                            "مقطع تحصیلی",
                                            educationLevels.map(el => ({ value: el.name, label: el.name })),
                                            ${formVar}.targetEducationLevel,
                                            (val) => ${setFormFn}({ ...${formVar}, targetEducationLevel: val })
                                        )}
                                    </div>`;
}

const lines = content.split('\n');

let inChannelForm = false;
let inGroupForm = false;
let replaceStart = -1;
let replaceEnd = -1;

for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('فیلترهای مخاطبین — همه فیلدها اختیاری هستند و مستقل از هم عمل می‌کنند. هرچه کمتر پر کنید، عمومی‌تر خواهد بود')) {
        // Find the start of the grid
        for (let j = i + 1; j < lines.length; j++) {
            if (lines[j].includes('<div className="grid grid-cols-1 md:grid-cols-2 gap-4">')) {
                replaceStart = j;
                // Find the end of the grid
                // It ends before `</div>\n                                </div>\n                            )}\n                            <div className="flex gap-2 justify-end pt-4">`
                // Let's just track the div matching
                let depth = 0;
                for (let k = j; k < lines.length; k++) {
                    if (lines[k].includes('<div ')) depth++;
                    if (lines[k].includes('<div>') || lines[k].includes('<div\n') || lines[k].match(/<div$/)) depth++;
                    if (lines[k].includes('</div')) depth--;
                    
                    // We know the exact end is right before `</div>\n                                </div>` block for the container
                    // Actually, let's just find `                                    </div>` which is the closing tag of this grid!
                    if (lines[k].includes('</div>') && depth === 0) {
                        replaceEnd = k;
                        break;
                    }
                }
                
                // If it's the first one, it's channel, second is group
                if (!inChannelForm) {
                    const newBlock = generateTargetingFields('channelForm');
                    lines.splice(replaceStart, replaceEnd - replaceStart + 1, newBlock);
                    inChannelForm = true;
                } else if (!inGroupForm) {
                    const newBlock = generateTargetingFields('groupForm');
                    lines.splice(replaceStart, replaceEnd - replaceStart + 1, newBlock);
                    inGroupForm = true;
                }
                break;
            }
        }
    }
}

fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log('MultiSelect UI injected successfully: ', inChannelForm, inGroupForm);
