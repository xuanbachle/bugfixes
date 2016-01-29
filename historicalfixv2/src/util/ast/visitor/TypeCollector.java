package util.ast.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.Set;

public class TypeCollector extends ASTVisitor
{
	private Set<String> typeSet;
	
	public TypeCollector(Set<String> o)
	{
		this.typeSet = o;
	}
	
	@Override
	public boolean visit(SimpleName node)
	{
		IBinding nodeBinding = node.resolveBinding();
		ITypeBinding typeBinding = node.resolveTypeBinding();
		
		if(nodeBinding != null && typeBinding != null && 
				nodeBinding instanceof IVariableBinding &&
				!typeBinding.isPrimitive())
		{
			String type = typeBinding.getQualifiedName();
			
			if(!type.equals("java.lang.String"))
				typeSet.add(type);
		}
		
		return super.visit(node);
	}
}
